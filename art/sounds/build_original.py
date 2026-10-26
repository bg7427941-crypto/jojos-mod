#!/usr/bin/env python3
"""Synthesize original Star Platinum effects offline; needs FFmpeg and FFprobe.

All excitation is generated here from seeded noise and mathematical oscillators.
No recordings, downloaded assets, voice models, or third-party sounds are used.
The generator and generated sounds use the repository's MIT license.
"""

import argparse
from array import array
import hashlib
import json
import math
from pathlib import Path
import random
import shutil
import subprocess
import sys
import tempfile
import wave


ROOT = Path(__file__).resolve().parents[2]
DESTINATION = ROOT / "src/main/resources/assets/jojosmod/sounds/star_platinum/original"
MANIFEST = Path(__file__).with_name("original-provenance.json")
RATE = 48000
TARGET_PEAK = -4.0
TAU = 2.0 * math.pi
CLIPS = {
    "summon": {
        "duration": 0.74, "seed": 261101,
        "recipe": "Rising filtered air and flutter; pressure arrival at 0.43 s; damped inharmonic body and three short reflections.",
    },
    "dismiss": {
        "duration": 0.32, "seed": 261102,
        "recipe": "Two descending exhalations with falling filter cutoff and a soft dissipating low pressure mode.",
    },
    "swing_1": {
        "duration": 0.11, "seed": 261103,
        "recipe": "Broad midrange air arc, rapid forward sweep, soft low pressure displacement; asymmetric attack and release.",
    },
    "swing_2": {
        "duration": 0.118, "seed": 261104,
        "recipe": "Lower reverse air arc with early peak and a short trailing eddy; independent noise excitation.",
    },
    "impact_1": {
        "duration": 0.18, "seed": 261105,
        "recipe": "Dense low thump, 13 ms dry crack, falling 132-to-58 Hz pressure mode, and three damped body modes.",
    },
    "impact_2": {
        "duration": 0.19, "seed": 261106,
        "recipe": "Tighter high-mid crack, staggered low body impulse, falling 159-to-67 Hz mode and a short debris rattle.",
    },
    "guard": {
        "duration": 0.32, "seed": 261107,
        "recipe": "Inward air brace, two soft excitation impulses and five inharmonic damped metal modes, low pressure support.",
    },
    "guard_block": {
        "duration": 0.18, "seed": 261108,
        "recipe": "Dry contact transient, four short inharmonic metal modes, low body impact and a second tiny contact at 19 ms.",
    },
    "barrage_start": {
        "duration": 1.38, "seed": 261109,
        "recipe": "Pressure buildup into a turbulent rush; eight air accents at 0.20 + n * 0.15 s, with alternating broad resonances.",
        "accent_times_seconds": [round(0.20 + n * 0.15, 2) for n in range(8)],
    },
    "star_finger": {
        "duration": 0.82, "seed": 261110,
        "recipe": "Elastic three-mode extension under a gliding air formant, initial tension snap and a soft recoil at 0.56 s.",
    },
    "intercept": {
        "duration": 0.10, "seed": 261111,
        "recipe": "Brief rising bright air tension, three tiny precision resonances and a rapid smooth release; no sustained tail.",
    },
    "intercept_catch": {
        "duration": 0.18, "seed": 261112,
        "recipe": "Dry arrow-like noise snap, short shaft modes and a light high-metal/air afterimage; no heavy guard impact.",
    },
    "leap": {
        "duration": 0.28, "seed": 261113,
        "recipe": "Immediate compact low pressure push under a rising air formant and short bright upward rush; smoothly tapered release.",
    },
    "leap_land": {
        "duration": 0.20, "seed": 261114,
        "recipe": "Controlled low landing thud with damped body modes, soft contact noise and a short warm resonant energy tail.",
    },
}


def frames(duration):
    return round(duration * RATE)


def smoothstep(value):
    value = max(0.0, min(1.0, value))
    return value * value * (3.0 - 2.0 * value)


def envelope(t, duration, attack=0.003, release=0.025):
    if t < 0.0 or t >= duration:
        return 0.0
    return smoothstep(t / attack) * smoothstep((duration - t) / release)


def biquad(samples, kind, frequency, q=0.707):
    """RBJ filters; frequency may be a smooth function of seconds."""
    output = []
    x1 = x2 = y1 = y2 = 0.0
    moving = callable(frequency)
    for i, sample in enumerate(samples):
        # A 0.33 ms control step keeps a moving formant smooth and inexpensive.
        if i == 0 or (moving and i % 16 == 0):
            hz = frequency(i / RATE) if moving else frequency
            omega = TAU * max(25.0, min(10000.0, hz)) / RATE
            cosine = math.cos(omega)
            alpha = math.sin(omega) / (2.0 * q)
            a0, a1, a2 = 1.0 + alpha, -2.0 * cosine, 1.0 - alpha
            if kind == "lowpass":
                b0 = b2 = (1.0 - cosine) / 2.0
                b1 = 1.0 - cosine
            elif kind == "highpass":
                b0 = b2 = (1.0 + cosine) / 2.0
                b1 = -1.0 - cosine
            else:
                b0, b1, b2 = alpha, 0.0, -alpha
            b0, b1, b2, a1, a2 = (v / a0 for v in (b0, b1, b2, a1, a2))
        value = b0 * sample + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1, y2, y1 = x1, sample, y1, value
        output.append(value)
    return output


def noise_band(rng, duration, low=100.0, high=5000.0, center=None, q=0.7):
    signal = [rng.random() * 2.0 - 1.0 for _ in range(frames(duration))]
    signal = biquad(signal, "highpass", low)
    signal = biquad(signal, "lowpass", high)
    if center is not None:
        signal = biquad(signal, "bandpass", center, q)
    rms = math.sqrt(sum(x * x for x in signal) / len(signal))
    return [x / rms for x in signal]


def mix(target, source, start=0.0, gain=1.0, shape=None):
    offset = frames(start)
    for i in range(max(0, -offset), min(len(source), len(target) - offset)):
        amount = shape(i / RATE) if shape else 1.0
        target[i + offset] += source[i] * gain * amount


def air(target, rng, start, duration, gain, center, attack=0.01,
        release=0.03, low=100.0, high=5200.0, shape=None, q=0.65):
    source = noise_band(rng, duration, low, high, center, q)
    mix(target, source, start, gain,
        lambda t: envelope(t, duration, attack, release) * (shape(t) if shape else 1.0))


def pressure(target, start, duration, gain, initial, final, decay, attack=0.002):
    """Low falling pressure mode: integrated exponential pitch, no laser tones."""
    phase = 0.0
    source = []
    for i in range(frames(duration)):
        t = i / RATE
        hz = final + (initial - final) * math.exp(-t / 0.025)
        phase += TAU * hz / RATE
        value = math.sin(phase) + 0.16 * math.sin(2.13 * phase + 0.7)
        source.append(value * math.exp(-t / decay) * envelope(t, duration, attack, 0.025))
    mix(target, source, start, gain)


def modes(target, start, duration, gain, frequencies, decay):
    """Inharmonic damped modes mimic a struck body rather than a sustained note."""
    source = [0.0] * frames(duration)
    for k, hz in enumerate(frequencies):
        for i in range(len(source)):
            t = i / RATE
            damping = math.exp(-t / (decay / (1.0 + 0.24 * k)))
            bending = 0.018 * math.sin(TAU * 31.0 * t) * math.exp(-t / 0.035)
            source[i] += (math.sin(TAU * hz * t + bending) * damping
                          / (1.0 + 0.65 * k))
    mix(target, source, start, gain,
        lambda t: envelope(t, duration, 0.0015, min(0.03, duration * 0.3)))


def make_summon(out, rng):
    air(out, rng, 0.0, 0.51, 0.33, lambda t: 280.0 + 2600.0 * (t / 0.51) ** 1.8,
        0.025, 0.06, shape=lambda t: (t / 0.51) ** 1.1)
    air(out, rng, 0.10, 0.47, 0.13, lambda t: 1600.0 + 650.0 * math.sin(t * 10.0),
        0.09, 0.12, low=650.0, high=5800.0,
        shape=lambda t: 0.7 + 0.3 * math.sin(TAU * (21.0 * t + 12.0 * t * t)))
    air(out, rng, 0.424, 0.23, 0.52, 820.0, 0.003, 0.09,
        shape=lambda t: math.exp(-t / 0.065))
    pressure(out, 0.428, 0.29, 0.82, 124.0, 52.0, 0.095)
    modes(out, 0.433, 0.26, 0.18, [291.0, 463.0, 739.0, 1103.0], 0.105)
    # Sparse reflections add size without making a long, overlapping reverb.
    arrival = out[frames(0.42):frames(0.65)]
    for delay, level in [(0.013, 0.16), (0.029, -0.09), (0.047, 0.045)]:
        mix(out, arrival, 0.42 + delay, level)


def make_dismiss(out, rng):
    air(out, rng, 0.0, 0.30, 0.48, lambda t: 1650.0 * math.exp(-t / 0.13) + 180.0,
        0.009, 0.08, low=140.0, high=4500.0,
        shape=lambda t: math.exp(-t / 0.17))
    air(out, rng, 0.042, 0.26, 0.15, lambda t: 900.0 - 600.0 * t / 0.26,
        0.045, 0.07, high=2600.0)
    pressure(out, 0.0, 0.25, 0.19, 117.0, 43.0, 0.075, 0.006)


def make_swing(out, rng, variant):
    duration = len(out) / RATE
    if variant == 1:
        center = lambda t: 620.0 + 1550.0 * math.sin(math.pi * t / duration) ** 1.2
        shape = lambda t: math.sin(math.pi * t / duration) ** 1.8
        gain, attack, low = 0.60, 0.009, 180.0
    else:
        center = lambda t: 1800.0 * math.exp(-t / 0.055) + 400.0
        shape = lambda t: math.sin(math.pi * (t / duration) ** 0.65) ** 1.7
        gain, attack, low = 0.56, 0.006, 140.0
    air(out, rng, 0.0, duration, gain, center, attack, 0.02, low, 4800.0, shape)
    air(out, rng, 0.008, duration - 0.012, 0.12, 380.0, 0.014, 0.025,
        low=90.0, high=1400.0, shape=lambda t: math.sin(math.pi * t / duration) ** 2)
    pressure(out, 0.018, 0.06, 0.08, 150.0 if variant == 1 else 128.0, 75.0, 0.018)


def make_impact(out, rng, variant):
    deep = variant == 1
    air(out, rng, 0.0, 0.057 if deep else 0.043, 0.65, 1800.0 if deep else 2600.0,
        0.0012, 0.018, low=500.0, high=6800.0,
        shape=lambda t: math.exp(-t / (0.013 if deep else 0.010)))
    air(out, rng, 0.004, 0.13, 0.44, 240.0 if deep else 330.0,
        0.002, 0.04, low=55.0, high=1700.0,
        shape=lambda t: math.exp(-t / 0.034))
    pressure(out, 0.003 if deep else 0.008, 0.165, 0.95, 132.0 if deep else 159.0,
             58.0 if deep else 67.0, 0.049 if deep else 0.040)
    modes(out, 0.004, 0.14, 0.20,
          [174.0, 293.0, 481.0] if deep else [213.0, 367.0, 641.0], 0.038)
    if not deep:
        for start, gain in [(0.029, 0.12), (0.044, 0.08)]:
            air(out, rng, start, 0.022, gain, 1500.0, 0.001, 0.01, high=4400.0,
                shape=lambda t: math.exp(-t / 0.006))


def make_guard(out, rng):
    air(out, rng, 0.0, 0.20, 0.27, lambda t: 2200.0 - 1500.0 * t / 0.20,
        0.025, 0.08, low=230.0, high=5000.0)
    for start, gain in [(0.028, 0.26), (0.083, 0.13)]:
        air(out, rng, start, 0.038, gain, 1450.0, 0.002, 0.02,
            shape=lambda t: math.exp(-t / 0.010))
    modes(out, 0.033, 0.27, 0.26, [263.0, 679.0, 1049.0, 1649.0, 2317.0], 0.095)
    pressure(out, 0.03, 0.22, 0.36, 133.0, 78.0, 0.06, 0.008)


def make_guard_block(out, rng):
    air(out, rng, 0.0, 0.048, 0.65, 2500.0, 0.001, 0.02,
        low=450.0, high=7300.0, shape=lambda t: math.exp(-t / 0.011))
    modes(out, 0.002, 0.16, 0.24, [415.0, 1118.0, 1777.0, 2789.0], 0.042)
    pressure(out, 0.002, 0.15, 0.62, 150.0, 81.0, 0.032)
    air(out, rng, 0.019, 0.025, 0.13, 1700.0, 0.001, 0.014,
        shape=lambda t: math.exp(-t / 0.007))


def make_intercept(out, rng):
    air(out, rng, 0.0, 0.082, 0.36, lambda t: 2100.0 + 2500.0 * t / 0.082,
        0.003, 0.032, low=850.0, high=7600.0,
        shape=lambda t: math.exp(-t / 0.038))
    air(out, rng, 0.010, 0.072, 0.12, 5100.0, 0.008, 0.030,
        low=1900.0, high=7800.0, shape=lambda t: math.exp(-t / 0.028))
    modes(out, 0.006, 0.067, 0.075, [1511.0, 2381.0, 3643.0], 0.016)


def make_intercept_catch(out, rng):
    air(out, rng, 0.0, 0.028, 0.65, 3800.0, 0.0008, 0.012,
        low=1050.0, high=7800.0, shape=lambda t: math.exp(-t / 0.0045))
    modes(out, 0.0015, 0.075, 0.21, [783.0, 1969.0, 3527.0, 5113.0], 0.014)
    # Thin bright metal and displaced air leave a short golden afterimage.
    modes(out, 0.014, 0.136, 0.055, [1423.0, 2897.0, 4591.0], 0.036)
    air(out, rng, 0.008, 0.145, 0.15, lambda t: 4200.0 - 2700.0 * t / 0.145,
        0.003, 0.048, low=700.0, high=7200.0,
        shape=lambda t: math.exp(-t / 0.032))


def make_leap(out, rng):
    pressure(out, 0.0, 0.155, 0.85, 150.0, 48.0, 0.045, 0.001)
    air(out, rng, 0.0, 0.075, 0.42, 720.0, 0.001, 0.025,
        low=70.0, high=3200.0, shape=lambda t: math.exp(-t / 0.018))
    air(out, rng, 0.006, 0.247, 0.43,
        lambda t: 600.0 + 2900.0 * smoothstep(t / 0.247),
        0.005, 0.065, low=180.0, high=6500.0,
        shape=lambda t: math.exp(-t / 0.13) * (0.6 + 0.4 * smoothstep(t / 0.09)))
    air(out, rng, 0.048, 0.19, 0.11, 4300.0, 0.020, 0.065,
        low=1900.0, high=7600.0, shape=lambda t: math.exp(-t / 0.09))


def make_leap_land(out, rng):
    pressure(out, 0.0, 0.174, 0.88, 97.0, 46.0, 0.042, 0.002)
    air(out, rng, 0.001, 0.09, 0.32, 180.0, 0.0018, 0.034,
        low=48.0, high=1400.0, shape=lambda t: math.exp(-t / 0.020))
    modes(out, 0.002, 0.135, 0.20, [137.0, 229.0, 361.0], 0.036)
    modes(out, 0.012, 0.164, 0.095, [329.0, 493.0, 751.0], 0.045)
    air(out, rng, 0.020, 0.153, 0.07, 850.0, 0.008, 0.065,
        low=270.0, high=2800.0, shape=lambda t: math.exp(-t / 0.048))


def make_barrage_start(out, rng):
    air(out, rng, 0.0, 0.245, 0.22, lambda t: 220.0 + 1500.0 * (t / 0.245) ** 1.5,
        0.025, 0.04, low=85.0, high=3800.0, shape=lambda t: (t / 0.245) ** 0.8)
    air(out, rng, 0.15, 1.20, 0.13, lambda t: 680.0 + 200.0 * math.sin(t * 16.0),
        0.05, 0.11, low=120.0, high=3300.0,
        shape=lambda t: 0.64 + 0.16 * math.sin(TAU * t / 0.15))
    for n, start in enumerate(CLIPS["barrage_start"]["accent_times_seconds"]):
        air(out, rng, start, 0.115, 0.31 + 0.012 * n,
            lambda t, n=n: (1450.0 if n % 2 == 0 else 1800.0) - 850.0 * t / 0.115,
            0.005, 0.04, low=150.0, high=4600.0,
            shape=lambda t: math.sin(math.pi * (t / 0.115) ** 0.65) ** 1.6)
        pressure(out, start + 0.009, 0.09, 0.115, 123.0 + n * 2.0, 72.0, 0.025)


def make_star_finger(out, rng):
    air(out, rng, 0.0, 0.065, 0.30, 1600.0, 0.002, 0.025,
        shape=lambda t: math.exp(-t / 0.015))
    air(out, rng, 0.024, 0.66, 0.29,
        lambda t: 430.0 + 1700.0 * smoothstep(t / 0.43) - 640.0 * smoothstep((t - 0.43) / 0.23),
        0.035, 0.13, low=180.0, high=4800.0,
        shape=lambda t: (0.82 + 0.18 * math.sin(TAU * 19.0 * t)) * math.exp(-t / 0.70))
    # Three detuned, damped spring modes bend smoothly under the air layer.
    source = []
    phases = [0.0, 0.7, 1.9]
    for i in range(frames(0.69)):
        t = i / RATE
        hz = 162.0 + 120.0 * smoothstep(t / 0.34) - 82.0 * smoothstep((t - 0.34) / 0.35)
        hz *= 1.0 + 0.045 * math.sin(TAU * 10.0 * t) * math.exp(-t / 0.30)
        value = 0.0
        for k, ratio in enumerate([1.0, 1.47, 2.19]):
            phases[k] += TAU * hz * ratio / RATE
            value += math.sin(phases[k]) * math.exp(-t / (0.34 - 0.065 * k)) / (1.0 + k)
        source.append(value * envelope(t, 0.69, 0.012, 0.11))
    mix(out, source, 0.032, 0.19)
    air(out, rng, 0.56, 0.24, 0.16, lambda t: 1700.0 - 1100.0 * t / 0.24,
        0.012, 0.095, high=3700.0, shape=lambda t: math.exp(-t / 0.085))


def synthesize(name, spec):
    out = [0.0] * frames(spec["duration"])
    rng = random.Random(spec["seed"])
    if name.startswith("swing_"):
        make_swing(out, rng, int(name[-1]))
    elif name.startswith("impact_"):
        make_impact(out, rng, int(name[-1]))
    else:
        globals()["make_" + name](out, rng)
    out = biquad(biquad(out, "highpass", 34.0), "lowpass", 8200.0)
    # A short silent guard after the taper absorbs Vorbis transform ringing.
    out = [math.tanh(x * 0.62) / 0.62 * envelope(i / RATE, spec["duration"] - 0.008, 0.001, 0.022) ** 2
           for i, x in enumerate(out)]
    gain = 10.0 ** (-6.0 / 20.0) / max(abs(x) for x in out)
    return [x * gain for x in out]


def run(command):
    return subprocess.run(command, check=True, capture_output=True)


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_wave(path, samples):
    with wave.open(str(path), "wb") as output:
        output.setparams((1, 3, RATE, len(samples), "NONE", "not compressed"))
        output.writeframes(b"".join(round(x * 8388607).to_bytes(3, "little", signed=True) for x in samples))


def decoded_samples(path):
    result = run(["ffmpeg", "-v", "error", "-nostdin", "-i", str(path),
                  "-f", "f32le", "-c:a", "pcm_f32le", "-"])
    values = array("f")
    values.frombytes(result.stdout)
    if sys.byteorder != "little":
        values.byteswap()
    if not values or not all(math.isfinite(x) for x in values):
        raise RuntimeError(f"Missing or non-finite decoded audio: {path}")
    return values


def db(value):
    return 20.0 * math.log10(max(value, 1e-12))


def build(destination, manifest_path):
    destination.mkdir(parents=True, exist_ok=True)
    records = []
    with tempfile.TemporaryDirectory(prefix="jojosmod-original-audio-") as directory:
        temporary = Path(directory)
        for name, spec in CLIPS.items():
            source = temporary / (name + ".wav")
            write_wave(source, synthesize(name, spec))
            output = destination / (name + ".ogg")
            gain = 2.0
            lower, upper = -6.0, 6.0
            best = None
            for attempt in range(10):
                # Ogg bitexact is an OUTPUT option: it fixes the stream serial.
                run(["ffmpeg", "-v", "error", "-nostdin", "-y", "-i", str(source),
                     "-map_metadata", "-1", "-af", f"volume={gain:.6f}dB",
                     "-ar", str(RATE), "-ac", "1", "-c:a", "libvorbis", "-q:a", "6",
                     "-flags:a", "+bitexact", "-fflags", "+bitexact", str(output)])
                decoded = decoded_samples(output)
                peak = db(max(abs(x) for x in decoded))
                difference = abs(peak - TARGET_PEAK)
                if best is None or difference < best[0]:
                    best = (difference, output.read_bytes(), decoded, peak, gain)
                if abs(peak - TARGET_PEAK) <= 0.08:
                    break
                # Quantization can make peak-vs-gain discontinuous. Keep the
                # closest encoded result instead of oscillating around a step.
                if peak < TARGET_PEAK:
                    lower = max(lower, gain)
                else:
                    upper = min(upper, gain)
                gain = (lower + upper) / 2.0
            _, encoded, decoded, peak, gain = best
            output.write_bytes(encoded)
            probe = json.loads(run(["ffprobe", "-v", "error", "-show_streams", "-show_format",
                                    "-of", "json", str(output)]).stdout)
            stream = probe["streams"][0]
            duration = float(probe["format"]["duration"])
            tail_peak = db(max(abs(x) for x in decoded[-frames(0.005):]))
            if (stream["codec_name"] != "vorbis" or stream["channels"] != 1
                    or int(stream["sample_rate"]) != RATE):
                raise RuntimeError(f"Unexpected format: {output}")
            if abs(duration - spec["duration"]) > 1.0 / RATE:
                raise RuntimeError(f"Unexpected duration: {output}: {duration}")
            if abs(peak - TARGET_PEAK) > 0.45 or tail_peak > -40.0:
                raise RuntimeError(f"Peak/tail check failed: {output}: {peak}, {tail_peak} dBFS")
            record = {
                "file": "star_platinum/original/" + output.name,
                "license": "MIT", "seed": spec["seed"], "recipe": spec["recipe"],
                "duration_seconds": round(duration, 6), "codec": "vorbis",
                "channels": 1, "sample_rate": RATE, "vorbis_quality": 6,
                "source_frames": frames(spec["duration"]), "decoded_frames": len(decoded),
                "normalization_gain_db": round(gain, 6), "decoded_peak_dbfs": round(peak, 3),
                "decoded_rms_dbfs": round(db(math.sqrt(sum(x * x for x in decoded) / len(decoded))), 3),
                "decoded_last_5ms_peak_dbfs": round(tail_peak, 3), "sha256": sha256(output),
            }
            if "accent_times_seconds" in spec:
                record["accent_times_seconds"] = spec["accent_times_seconds"]
            records.append(record)
            print(f"{name:14} {duration:.3f} s | mono {RATE} Hz | peak {peak:.3f} dBFS | tail {tail_peak:.1f} dBFS", flush=True)
    manifest = {
        "collection": "Original Star Platinum synthesized sound effects",
        "author": "bg7427941-crypto project contributors", "license": "MIT",
        "license_file": "LICENSE", "generator": "art/sounds/build_original.py",
        "generator_sha256": sha256(Path(__file__)), "recorded_inputs": [],
        "external_audio_inputs": [], "voice_inputs": [],
        "synthesis": "Seeded Python noise; RBJ filters; damped pressure and inharmonic resonator modes; mild saturation; 24-bit PCM intermediate.",
        "mastering": "34 Hz highpass, 8.2 kHz lowpass, smooth edge fades and 8 ms final silence, decoded Vorbis peak normalization to -4 dBFS.",
        "python": sys.version.split()[0],
        "encoder": run(["ffmpeg", "-version"]).stdout.decode().splitlines()[0],
        "determinism": "Fixed seeds and sample counts; no network, input media, or timestamps; output-side FFmpeg +bitexact fixes Ogg serial. Byte reproducible with the same Python/FFmpeg toolchain.",
        "clips": records,
    }
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Regenerate in a temporary directory and compare every OGG and the manifest")
    arguments = parser.parse_args()
    for tool in ("ffmpeg", "ffprobe"):
        if shutil.which(tool) is None:
            parser.error(f"{tool} is required in PATH")
    if arguments.check:
        with tempfile.TemporaryDirectory(prefix="jojosmod-original-check-") as directory:
            temporary = Path(directory)
            build(temporary / "sounds", temporary / "manifest.json")
            for name in CLIPS:
                if (temporary / "sounds" / (name + ".ogg")).read_bytes() != (DESTINATION / (name + ".ogg")).read_bytes():
                    raise SystemExit(f"Reproducibility mismatch: {name}.ogg")
            if (temporary / "manifest.json").read_bytes() != MANIFEST.read_bytes():
                raise SystemExit("Reproducibility mismatch: original-provenance.json")
            print(f"Reproducibility OK: all {len(CLIPS)} OGG files and manifest are byte-identical.")
    else:
        build(DESTINATION, MANIFEST)


if __name__ == "__main__":
    main()
