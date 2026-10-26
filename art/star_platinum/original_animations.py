"""Original Star Platinum motion for the hand-built rig.

Authored poses use X=-90 for a forward arm, with Y/Z in the unreflected
sculpting coordinates. Export compensates Y/Z for GeckoLib's X reflection.
No geometry or animation from
another asset is loaded here. All movement is authored below in degrees and
model pixels; the only external constraints are the server action times.
"""

from __future__ import annotations

from bisect import bisect_right
from copy import deepcopy
from math import cos, isfinite, radians, sin


BONES = (
    "root", "hover", "pelvis", "waist", "chest", "neck", "head", "hair",
    "scarf", "scarf_tail_left", "scarf_tail_right",
    "left_upper_arm", "left_forearm", "left_hand",
    "right_upper_arm", "right_forearm", "right_hand",
    "right_finger_index", "right_finger_middle",
    "loin_front", "loin_back", "loin_left", "loin_right",
    "left_thigh", "left_shin", "left_foot",
    "right_thigh", "right_shin", "right_foot",
)
FINGERS = ("right_finger_index", "right_finger_middle")
BARRAGE_CONTACTS = tuple(round(0.2 + index * 0.15, 2) for index in range(8))
CLIP_LENGTHS = {
    "idle": 2.4, "move": 1.2, "summon": 0.4, "dismiss": 0.2,
    "strike": 0.6, "barrage": 1.5, "guard": 0.8, "recover": 0.22,
    "star_finger": 0.9, "intercept": 0.6, "intercept_catch": 0.22,
}


def _ready():
    """A staggered hover: loose shoulders, forward fists, one knee advanced."""
    pose = {bone: {"rotation": [0, 0, 0]} for bone in BONES}
    rotations = {
        "pelvis": [0, 0, 2], "waist": [0, -5, -2],
        "chest": [-3, 7, -1], "neck": [2, -3, 1], "head": [0, -3, 0],
        "hair": [3, 0, -1], "scarf": [-2, 0, 0],
        "scarf_tail_left": [14, -6, 4], "scarf_tail_right": [18, 7, -5],
        "left_upper_arm": [-26, -8, 8], "left_forearm": [-47, 0, -2],
        "left_hand": [5, -4, -3],
        "right_upper_arm": [-16, 9, -10], "right_forearm": [-56, 0, 2],
        "right_hand": [3, 5, 3],
        "loin_front": [-13, 0, 0], "loin_back": [16, 0, 0],
        "loin_left": [0, -3, 8], "loin_right": [0, 3, -9],
        "left_thigh": [-12, -4, 5], "left_shin": [21, 0, 0],
        "left_foot": [-7, 0, -2],
        "right_thigh": [11, 6, -6], "right_shin": [22, 0, 0],
        "right_foot": [-17, 0, 2],
    }
    for bone, value in rotations.items():
        pose[bone]["rotation"] = value
    pose["hover"]["position"] = [0, 1.2, 0]
    for finger in FINGERS:
        pose[finger]["scale"] = [0, 0, 0]
    return pose


def _pose(base=None, *, rotation=None, position=None, scale=None):
    pose = deepcopy(_ready() if base is None else base)
    for channel, values in (("rotation", rotation), ("position", position), ("scale", scale)):
        for bone, vector in (values or {}).items():
            if bone not in BONES:
                raise ValueError(f"Unknown rig bone: {bone}")
            pose[bone][channel] = list(vector)
    return pose


def _time(value):
    return f"{value:.5f}".rstrip("0").rstrip(".") or "0"


def _clip(name, knots, *, loop=False):
    """Bake eased authored poses, retaining every exact authored timestamp.

    Linear interpolation between short samples avoids spline overshoot at
    contacts, elbows, cloth and finger scale. Identical tracks stay constant.
    """
    length = CLIP_LENGTHS[name]
    authored_times = [time for time, _ in knots]
    if authored_times[0] != 0 or authored_times[-1] != length:
        raise ValueError(f"{name}: authored poses must span the entire clip")
    if any(a >= b for a, b in zip(authored_times, authored_times[1:])):
        raise ValueError(f"{name}: pose times must increase")
    times = sorted(set(authored_times + [round(index / 40, 5)
                                        for index in range(int(length * 40) + 1)]))
    frames = []
    for time in times:
        segment = min(bisect_right(authored_times, time) - 1, len(knots) - 2)
        start, first = knots[segment]
        end, last = knots[segment + 1]
        fraction = (time - start) / (end - start)
        eased = fraction * fraction * (3 - 2 * fraction)
        frame = {}
        for bone in BONES:
            frame[bone] = {}
            for channel in sorted(first[bone].keys() | last[bone].keys()):
                neutral = [1, 1, 1] if channel == "scale" else [0, 0, 0]
                before = first[bone].get(channel, neutral)
                after = last[bone].get(channel, neutral)
                frame[bone][channel] = [round(a + (b - a) * eased, 5)
                                        for a, b in zip(before, after)]
        frames.append(frame)
    bones = {}
    for bone in BONES:
        bones[bone] = {}
        for channel in frames[0][bone]:
            values = [frame[bone][channel] for frame in frames]
            if channel == "rotation":
                # GeometryBone.bake mirrors the X pivot/vertices; rotation
                # baking alone (-X,-Y,+Z) omits that change of handedness.
                values = [[value[0], -value[1], -value[2]] for value in values]
            bones[bone][channel] = (values[0] if all(value == values[0] for value in values)
                                    else {_time(time): value for time, value in zip(times, values)})
    return {"loop": loop, "animation_length": length, "bones": bones}


def _idle():
    ready = _ready()
    inhale = _pose(ready, rotation={
        "pelvis": [0, 1, 2.5], "waist": [-1.5, -5, -2],
        "chest": [-4.5, 7, -1.5], "neck": [3, -3, 1],
        "left_upper_arm": [-27.5, -8, 8.5],
        "right_upper_arm": [-17, 9, -10.5],
        "left_thigh": [-11, -4, 5.5], "right_shin": [24, 0, 0],
    }, position={"hover": [0, 1.75, 0]})
    cloth_lag = _pose(inhale, rotation={
        "hair": [5, 0.8, -1], "scarf": [-3, 0, 0],
        "scarf_tail_left": [20, -8, 5], "scarf_tail_right": [23, 5, -6],
        "loin_front": [-15, 0, 0.5], "loin_back": [20, 0, -0.5],
    }, position={"hover": [0, 1.62, 0]})
    exhale = _pose(ready, rotation={
        "chest": [-2.5, 6.5, -0.5], "hair": [2, -0.5, -1],
        "scarf_tail_left": [12, -4, 3], "scarf_tail_right": [15, 8, -4],
        "loin_back": [14, 0, 0],
    }, position={"hover": [0, 0.95, 0]})
    return _clip("idle", [(0, ready), (0.8, inhale), (1.2, cloth_lag),
                           (1.9, exhale), (2.4, ready)], loop=True)


def _move():
    glide = _pose(rotation={
        "pelvis": [-3, 0, 2], "waist": [5, -4, -2], "chest": [8, 5, 0],
        "neck": [-7, -2, 0], "head": [-3, -2, 0],
        "hair": [10, 0, 0], "scarf": [4, 0, 0],
        "scarf_tail_left": [32, -9, 6], "scarf_tail_right": [38, 8, -7],
        "left_upper_arm": [-8, -9, 13], "left_forearm": [-53, 0, -3],
        "right_upper_arm": [5, 12, -15], "right_forearm": [-58, 0, 3],
        "loin_front": [-5, 0, 0], "loin_back": [30, 0, 0],
        "loin_left": [13, -3, 10], "loin_right": [16, 3, -11],
        "left_thigh": [15, -4, 5], "left_shin": [25, 0, 0],
        "left_foot": [-18, 0, -2],
        "right_thigh": [29, 6, -6], "right_shin": [31, 0, 0],
        "right_foot": [-23, 0, 2],
    }, position={"hover": [0, 1.35, 0]})
    rise = _pose(glide, rotation={
        "chest": [7, 6, -1], "hair": [13, -1, 0],
        "scarf_tail_left": [39, -6, 7], "scarf_tail_right": [33, 11, -8],
        "left_forearm": [-56, 0, -3], "right_thigh": [26, 6, -6],
        "right_shin": [34, 0, 0], "loin_back": [34, 0, 0],
    }, position={"hover": [0, 1.8, 0]})
    settle = _pose(glide, rotation={
        "chest": [9, 4, 1], "hair": [8, 1, 0],
        "scarf_tail_left": [29, -11, 5], "scarf_tail_right": [40, 5, -6],
        "left_thigh": [18, -4, 5], "left_shin": [23, 0, 0],
        "loin_back": [27, 0, 0],
    }, position={"hover": [0, 1.1, 0]})
    return _clip("move", [(0, glide), (0.35, rise), (0.85, settle),
                           (1.2, glide)], loop=True)


def _summon():
    ready = _ready()
    gather = _pose(ready, rotation={
        "pelvis": [-6, -8, 4], "waist": [8, 12, -4],
        "chest": [10, -8, 0], "neck": [-9, 0, 0], "head": [-7, 4, 0],
        "left_upper_arm": [-8, -10, 24], "left_forearm": [-34, 0, -5],
        "right_upper_arm": [2, 14, -21], "right_forearm": [-40, 0, 5],
        "left_thigh": [-30, -5, 9], "left_shin": [42, 0, 0],
        "right_thigh": [21, 9, -9], "right_shin": [39, 0, 0],
        "scarf_tail_left": [29, -12, 8], "scarf_tail_right": [34, 12, -8],
        "loin_front": [-28, 0, 0], "loin_back": [28, 0, 0],
    }, position={"hover": [0, -2.4, 0.8]})
    unfold = _pose(ready, rotation={
        "waist": [-2, -9, -2], "chest": [-5, 11, -1],
        "left_upper_arm": [-31, -9, 12], "left_forearm": [-43, 0, -2],
        "right_upper_arm": [-21, 11, -13], "right_forearm": [-51, 0, 2],
        "hair": [8, 0, -1], "scarf_tail_left": [23, -9, 7],
        "scarf_tail_right": [27, 10, -7], "loin_back": [23, 0, 0],
    }, position={"hover": [0, 1.55, 0]})
    return _clip("summon", [(0, gather), (0.26, unfold), (0.4, ready)])


def _dismiss():
    ready = _ready()
    release = _pose(ready, rotation={
        "waist": [2, -2, -1], "chest": [2, 2, -1], "neck": [-2, 0, 0],
        "left_upper_arm": [-12, -6, 10], "left_forearm": [-32, 0, -2],
        "right_upper_arm": [-6, 7, -11], "right_forearm": [-35, 0, 2],
        "left_thigh": [-4, -3, 4], "left_shin": [15, 0, 0],
        "right_thigh": [16, 5, -5], "right_shin": [26, 0, 0],
        "hair": [7, 0, -1], "scarf_tail_left": [24, -7, 5],
        "scarf_tail_right": [28, 8, -6],
    }, position={"hover": [0, -1.6, 1.5]})
    return _clip("dismiss", [(0, ready), (0.2, release)])


def _strike():
    ready = _ready()
    coil = _pose(ready, rotation={
        "pelvis": [0, -5, 3], "waist": [1, 10, -3],
        "chest": [-5, 18, -4], "neck": [3, -12, 2], "head": [0, -8, 0],
        "right_upper_arm": [-8, 18, -17], "right_forearm": [-112, 0, 2],
        "left_upper_arm": [-37, -12, 6], "left_forearm": [-65, 0, -3],
        "right_thigh": [18, 8, -7], "right_shin": [28, 0, 0],
        "scarf_tail_left": [20, -12, 5], "scarf_tail_right": [23, -1, -7],
    }, position={"hover": [0, 1.05, 0.4]})
    drive = _pose(coil, rotation={
        "waist": [3, -3, -2], "chest": [2, -2, -2], "neck": [-1, 2, 1],
        "head": [0, 0, 0], "right_upper_arm": [-57, 8, -8],
        "right_forearm": [-39, 0, 1], "right_hand": [0, 0, 0],
    }, position={"hover": [0, 1.15, -0.35]})
    contact = _pose(drive, rotation={
        "pelvis": [0, -5, 2], "waist": [3, -8, -2],
        "chest": [4, -14, -3], "neck": [-4, 12, 1], "head": [0, 9, 0],
        "right_upper_arm": [-90, 0, -3], "right_forearm": [0, 0, 0],
        "right_hand": [0, 0, 0],
        "left_upper_arm": [-33, -12, 7], "left_forearm": [-72, 0, -3],
        "left_thigh": [-18, -4, 5], "left_shin": [26, 0, 0],
        "right_thigh": [19, 8, -7], "right_shin": [31, 0, 0],
        "loin_front": [-19, 0, -1], "loin_back": [23, 0, 1],
    }, position={"hover": [0, 1.25, -1.0]})
    follow = _pose(contact, rotation={
        "chest": [4, -18, -2], "neck": [-4, 15, 0],
        "right_upper_arm": [-89, -2, -4], "right_forearm": [-8, 0, 0],
        "hair": [8, -4, 0], "scarf_tail_left": [28, 4, 7],
        "scarf_tail_right": [32, 16, -8],
    }, position={"hover": [0, 1.25, -1.05]})
    withdraw = _pose(ready, rotation={
        "waist": [1, -6, -2], "chest": [0, -3, -1], "neck": [0, 4, 1],
        "right_upper_arm": [-53, 5, -8], "right_forearm": [-35, 0, 2],
        "left_upper_arm": [-29, -9, 7], "left_forearm": [-54, 0, -2],
        "hair": [6, 1, -1], "scarf_tail_left": [23, -2, 5],
        "scarf_tail_right": [26, 12, -6],
    }, position={"hover": [0, 1.2, -0.3]})
    return _clip("strike", [(0, ready), (0.085, coil), (0.145, drive),
                             (0.2, contact), (0.255, follow),
                             (0.38, withdraw), (0.6, ready)])


def _barrage_contact(right):
    side = -1 if right else 1
    punching = "right" if right else "left"
    chambered = "left" if right else "right"
    pose = _pose(rotation={
        "pelvis": [0, side * 2, 0], "waist": [4, side * 4, 0],
        "chest": [5, side * 11, side * 2], "neck": [-5, side * -8, 0],
        "head": [0, side * -5, 0],
        f"{punching}_upper_arm": [-90, 0, side * 4],
        f"{punching}_forearm": [0, 0, 0], f"{punching}_hand": [0, 0, 0],
        f"{chambered}_upper_arm": [-27, side * 10, side * -12],
        f"{chambered}_forearm": [-87, 0, side * 3],
        f"{chambered}_hand": [4, side * 4, side * 3],
        "left_thigh": [-19, -4, 7], "left_shin": [29, 0, 0],
        "right_thigh": [16, 6, -7], "right_shin": [29, 0, 0],
        "loin_front": [-20, side * 2, 0], "loin_back": [25, 0, 0],
        "scarf_tail_left": [27, side * -8 - 4, 6],
        "scarf_tail_right": [31, side * -8 + 5, -7],
        "hair": [7, side * -3, 0],
    }, position={"hover": [0, 1.2, -0.8]})
    return pose


def _barrage():
    ready = _ready()
    gather = _pose(ready, rotation={
        "waist": [3, 5, -1], "chest": [1, 12, -2],
        "right_upper_arm": [-20, 12, -13], "right_forearm": [-98, 0, 3],
        "left_upper_arm": [-37, -9, 9], "left_forearm": [-72, 0, -3],
    }, position={"hover": [0, 1.05, 0.15]})
    knots = [(0, ready), (0.09, gather)]
    for index, time in enumerate(BARRAGE_CONTACTS):
        knots.append((time, _barrage_contact(index % 2 == 0)))
        if index < len(BARRAGE_CONTACTS) - 1:
            # Both elbows fold during the hand-off; the next fist leaves its
            # chamber as the previous fist passes back alongside the ribs.
            passing = _pose(rotation={
                "pelvis": [0, 0, 0], "waist": [4, 0, 0], "chest": [5, 0, 0],
                "neck": [-5, 0, 0], "head": [0, 0, 0],
                "left_upper_arm": [-53, -5, 9], "left_forearm": [-48, 0, -2],
                "right_upper_arm": [-53, 5, -9], "right_forearm": [-48, 0, 2],
                "left_hand": [2, 0, -2], "right_hand": [2, 0, 2],
                "left_thigh": [-18, -4, 7], "left_shin": [28, 0, 0],
                "right_thigh": [16, 6, -7], "right_shin": [28, 0, 0],
                "loin_front": [-20, 0, 0], "loin_back": [27, 0, 0],
                "scarf_tail_left": [31, -5, 7], "scarf_tail_right": [35, 6, -8],
                "hair": [8, 0, 0],
            }, position={"hover": [0, 1.1, -0.65]})
            knots.append((round(time + 0.075, 3), passing))
    settle = _pose(ready, rotation={
        "left_upper_arm": [-45, -5, 9], "left_forearm": [-39, 0, -2],
        "right_upper_arm": [-39, 7, -10], "right_forearm": [-43, 0, 2],
        "waist": [2, -3, -1], "chest": [1, 4, -1],
        "scarf_tail_left": [24, -6, 6], "scarf_tail_right": [29, 7, -7],
    }, position={"hover": [0, 1.1, -0.3]})
    knots.extend([(1.36, settle), (1.5, ready)])
    return _clip("barrage", knots)


def _guard():
    # A small outward shoulder roll leaves room for the elbows. Inward
    # forearm yaw brings the gloves together in front of the cheeks.
    guard = _pose(rotation={
        "pelvis": [0, 0, 0], "waist": [2, 0, 0], "chest": [4, 0, 0],
        "neck": [-5, 0, 0], "head": [-3, 0, 0],
        "left_upper_arm": [-84, -5, 15], "left_forearm": [-60, -20, -4],
        "left_hand": [4, -4, -2],
        "right_upper_arm": [-84, 5, -15], "right_forearm": [-60, 20, 4],
        "right_hand": [4, 4, 2],
        "left_thigh": [-18, -4, 7], "left_shin": [30, 0, 0],
        "right_thigh": [7, 6, -7], "right_shin": [31, 0, 0],
        "loin_front": [-20, 0, 0], "loin_back": [20, 0, 0],
        "scarf_tail_left": [17, -5, 4], "scarf_tail_right": [21, 7, -5],
    }, position={"hover": [0, 0.85, 0.1]})
    brace = _pose(guard, rotation={
        "waist": [2.5, 0, 0], "chest": [5, 0, 0], "neck": [-6, 0, 0],
        "left_upper_arm": [-85, -5, 15], "right_upper_arm": [-85, 5, -15],
        "left_shin": [32, 0, 0], "hair": [5, 0, -1],
        "scarf_tail_left": [20, -6, 5], "scarf_tail_right": [24, 6, -6],
    }, position={"hover": [0, 1.05, 0.2]})
    return _clip("guard", [(0, guard), (0.4, brace), (0.8, guard)], loop=True)


def _recover():
    # Combined shoulder/elbow pitch decreases throughout: fists lower from
    # the spent forward position without a second wind-up or upward flourish.
    spent = _pose(rotation={
        "waist": [3, -3, -1], "chest": [3, 2, -1], "neck": [-2, 0, 1],
        "left_upper_arm": [-46, -6, 9], "left_forearm": [-42, 0, -2],
        "right_upper_arm": [-42, 7, -10], "right_forearm": [-35, 0, 2],
        "hair": [7, 0, -1], "scarf_tail_left": [24, -5, 6],
        "scarf_tail_right": [28, 7, -7],
    }, position={"hover": [0, 1.0, -0.25]})
    return _clip("recover", [(0, spent), (0.22, _ready())])


def _star_finger():
    ready = _ready()
    sight = _pose(ready, rotation={
        "waist": [1, 4, -1], "chest": [-2, 11, -2],
        "neck": [1, -8, 1], "head": [0, -5, 0],
        "right_upper_arm": [-30, 10, -10], "right_forearm": [-98, 0, 2],
        "left_upper_arm": [-42, -10, -6], "left_forearm": [-90, 0, -3],
        "left_hand": [5, -4, -2],
    }, position={"hover": [0, 1.05, 0.15]})
    align = _pose(sight, rotation={
        "pelvis": [0, 0, 0], "waist": [0, 0, 0], "chest": [0, 0, 0],
        "neck": [-2, 6, 1], "head": [0, 5, 0],
        "right_upper_arm": [-90, 0, 0], "right_forearm": [0, 0, 0],
        "right_hand": [0, 0, 0],
        "left_upper_arm": [-84, -5, 15], "left_forearm": [-60, -20, -4],
        "left_hand": [4, -4, -2],
        "left_thigh": [-16, -4, 6], "left_shin": [25, 0, 0],
        "loin_front": [-18, 0, 0], "loin_back": [21, 0, 0],
    }, position={"hover": [0, 1.15, -0.45]})
    extend = _pose(align, scale={finger: [1, 80, 1] for finger in FINGERS})
    trail = _pose(extend, rotation={
        "hair": [7, -2, -1], "scarf_tail_left": [24, -1, 6],
        "scarf_tail_right": [28, 11, -7],
    })
    retract = _pose(trail, scale={finger: [1, 18, 1] for finger in FINGERS})
    hidden = _pose(trail, scale={finger: [0, 0, 0] for finger in FINGERS})
    lower = _pose(ready, rotation={
        "right_upper_arm": [-52, 5, -6], "right_forearm": [-33, 0, 1],
        "left_upper_arm": [-35, -8, 1], "left_forearm": [-59, 0, -2],
        "chest": [0, 0, -1], "neck": [0, 2, 1],
        "scarf_tail_left": [21, -4, 5], "scarf_tail_right": [25, 9, -6],
    }, position={"hover": [0, 1.15, -0.15]})
    return _clip("star_finger", [(0, ready), (0.14, sight), (0.29, align),
                                  (0.4, extend), (0.46, trail),
                                  (0.62, retract), (0.7, hidden),
                                  (0.79, lower), (0.9, ready)])


def _intercept_pose():
    """One presented catching hand, with the other hand protecting the ribs."""
    return _pose(rotation={
        "pelvis": [0, 0, 0], "waist": [1, -3, -1], "chest": [0, 3, 1],
        "neck": [-1, 6, 0], "head": [0, -6, 0],
        "right_upper_arm": [-78, 5, -8], "right_forearm": [-48, 12, 8],
        "right_hand": [-8, -10, 26],
        "left_upper_arm": [-23, -13, 11], "left_forearm": [-69, 0, -5],
        "left_hand": [5, -6, -4],
        "left_thigh": [-9, -6, 6], "left_shin": [18, 0, 0],
        "left_foot": [-5, 0, -2],
        "right_thigh": [14, 8, -7], "right_shin": [26, 0, 0],
        "right_foot": [-18, 0, 2],
        "hair": [4, 1, -1], "scarf": [-1, 0, 0],
        "scarf_tail_left": [18, -8, 5], "scarf_tail_right": [22, 6, -6],
        "loin_front": [-12, 0, 0], "loin_back": [19, 0, 0],
    }, position={"hover": [0, 1.25, -0.3]})


def _intercept():
    ready = _ready()
    lift = _pose(rotation={
        "pelvis": [0, 1, 1], "waist": [1, -5, -1], "chest": [-1, 6, 0],
        "neck": [0, 1, 0], "head": [0, -4, 0],
        "right_upper_arm": [-45, 8, -10], "right_forearm": [-52, 8, 5],
        "right_hand": [-10, -6, 18],
        "left_upper_arm": [-27, -11, 10], "left_forearm": [-64, 0, -4],
        "hair": [5, 1, -1], "scarf_tail_right": [24, 5, -6],
    }, position={"hover": [0, 1.15, -0.1]})
    presented = _intercept_pose()
    lower = _pose(rotation={
        "pelvis": [0, 0, 1], "waist": [0, -4, -1], "chest": [-1, 5, 0],
        "neck": [1, -1, 0], "head": [0, -3, 0],
        "right_upper_arm": [-48, 8, -9], "right_forearm": [-50, 7, 5],
        "right_hand": [2, 6, 8],
        "left_upper_arm": [-24, -10, 9], "left_forearm": [-60, 0, -3],
        "hair": [5, -1, -1], "scarf_tail_left": [20, -6, 5],
        "scarf_tail_right": [24, 8, -6],
    }, position={"hover": [0, 1.2, -0.12]})
    # Ticks 2..9 can catch an arrow. Keep the entire catching pose still
    # until the window closes at tick 10, then lower the hand to ready.
    return _clip("intercept", [(0, ready), (0.05, lift), (0.1, presented),
                               (0.5, presented), (0.55, lower), (0.6, ready)])


def _intercept_catch():
    presented = _intercept_pose()
    grip = _pose(presented, rotation={
        "waist": [2, -5, -1], "chest": [-1, 5, 1],
        "right_upper_arm": [-75, 6, -9], "right_forearm": [-54, 18, 4],
        "right_hand": [8, 18, -12],
        "neck": [-1, 4, 0], "head": [0, -4, 0],
    }, position={"hover": [0, 1.2, -0.22]})
    recoil = _pose(grip, rotation={
        "waist": [2, -7, -1], "chest": [-1, 7, 1],
        "right_upper_arm": [-68, 9, -10], "right_forearm": [-61, 15, 6],
        "right_hand": [10, 20, -16],
        "left_upper_arm": [-27, -12, 10], "left_forearm": [-65, 0, -4],
        "hair": [7, 2, -1], "scarf_tail_left": [23, -5, 6],
        "scarf_tail_right": [27, 9, -7],
    }, position={"hover": [0, 1.1, 0.05]})
    settle = _pose(rotation={
        "waist": [1, -6, -2], "chest": [-2, 7, -1],
        "right_upper_arm": [-38, 10, -11], "right_forearm": [-57, 0, 3],
        "right_hand": [5, 10, -2],
        "left_upper_arm": [-27, -10, 9], "left_forearm": [-56, 0, -3],
        "hair": [5, 1, -1], "scarf_tail_left": [20, -5, 5],
        "scarf_tail_right": [24, 8, -6],
    }, position={"hover": [0, 1.15, 0.02]})
    return _clip("intercept_catch", [(0, presented), (0.035, grip),
                                     (0.08, recoil), (0.14, settle),
                                     (0.22, _ready())])


def build_animations():
    """Return the complete numeric GeckoLib Bedrock animation document."""
    clips = {
        "idle": _idle(), "move": _move(), "summon": _summon(),
        "dismiss": _dismiss(), "strike": _strike(), "barrage": _barrage(),
        "guard": _guard(), "recover": _recover(), "star_finger": _star_finger(),
        "intercept": _intercept(), "intercept_catch": _intercept_catch(),
    }
    return {"format_version": "1.8.0", "animations": {
        f"animation.star_platinum.{name}": clip for name, clip in clips.items()
    }}


def _at(channel, time):
    return channel if isinstance(channel, list) else channel[_time(time)]


def _runtime_direction(bones, time, chain, vector):
    """Rotate in sculpting coordinates, including GeckoLib's X reflection."""
    vx, vy, vz = vector
    for bone in reversed(chain):
        angles = _at(bones[bone].get("rotation", [0, 0, 0]), time)
        x, y, z = (radians(angle * sign) for angle, sign in zip(angles, (-1, 1, -1)))
        vy, vz = vy * cos(x) - vz * sin(x), vy * sin(x) + vz * cos(x)
        vx, vz = vx * cos(y) + vz * sin(y), -vx * sin(y) + vz * cos(y)
        vx, vy = vx * cos(z) - vy * sin(z), vx * sin(z) + vy * cos(z)
    return [vx, vy, vz]


def _runtime_point(bones, time, chain, point):
    """Transform a rig point without loading geometry or a model library."""
    point = list(point)
    for bone, pivot in reversed(chain):
        local = [value - center for value, center in zip(point, pivot)]
        rotated = _runtime_direction(bones, time, (bone,), local)
        position = _at(bones[bone].get("position", [0, 0, 0]), time)
        point = [center + value + offset
                 for center, value, offset in zip(pivot, rotated, position)]
    return point


def audit_animations(document=None):
    """Raise on rig, channel, timing, loop, contact or extension regressions."""
    document = build_animations() if document is None else document
    assert document["format_version"] == "1.8.0"
    clips = document["animations"]
    assert set(clips) == {f"animation.star_platinum.{name}" for name in CLIP_LENGTHS}
    track_count = key_count = 0
    for name, length in CLIP_LENGTHS.items():
        clip = clips[f"animation.star_platinum.{name}"]
        assert clip["animation_length"] == length, name
        assert clip["loop"] is (name in {"idle", "move", "guard"}), name
        assert set(clip["bones"]) == set(BONES), name
        for bone, channels in clip["bones"].items():
            for channel, track in channels.items():
                assert channel in {"rotation", "position", "scale"}, (name, bone)
                track_count += 1
                if isinstance(track, dict):
                    times = [float(time) for time in track]
                    assert times == sorted(times) and times[0] == 0 and times[-1] == length
                    assert all(0 <= time <= length for time in times), (name, bone)
                    values = list(track.values())
                    if clip["loop"]:
                        assert values[0] == values[-1], (name, bone, "loop seam")
                else:
                    values = [track]
                key_count += len(values)
                for value in values:
                    assert len(value) == 3, (name, bone, channel)
                    assert all(type(component) in {int, float} and isfinite(component)
                               for component in value), (name, bone, channel)
                    if channel == "scale":
                        assert all(component >= 0 for component in value), (name, bone)
            if name != "star_finger" and bone in FINGERS:
                assert channels["scale"] == [0, 0, 0], (name, bone, "visible extension")
    strike = clips["animation.star_platinum.strike"]["bones"]
    assert _at(strike["right_upper_arm"]["rotation"], 0.2)[0] == -90
    assert _at(strike["right_forearm"]["rotation"], 0.2) == [0, 0, 0]
    barrage = clips["animation.star_platinum.barrage"]["bones"]
    for index, time in enumerate(BARRAGE_CONTACTS):
        side = "right" if index % 2 == 0 else "left"
        assert _at(barrage[f"{side}_upper_arm"]["rotation"], time)[0] == -90
        assert _at(barrage[f"{side}_forearm"]["rotation"], time) == [0, 0, 0]
    finger = clips["animation.star_platinum.star_finger"]["bones"]
    for bone in FINGERS:
        scales = finger[bone]["scale"]
        assert _at(scales, 0.4) == [1, 80, 1]
        assert _at(scales, 0.7) == [0, 0, 0]
        assert all(vector[1] <= 80 for vector in scales.values())
        assert all(vector == [0, 0, 0] for time, vector in scales.items() if float(time) >= 0.7)
        assert finger[bone]["rotation"] == [0, 0, 0]
        chain = ("root", "hover", "pelvis", "waist", "chest",
                 "right_upper_arm", "right_forearm", "right_hand", bone)
        direction = _runtime_direction(finger, 0.4, chain, [0, -1, 0])
        assert all(abs(actual - expected) < 1e-8
                   for actual, expected in zip(direction, [0, 0, -1])), (bone, direction)
    assert _at(finger["right_upper_arm"]["rotation"], 0.4) == [-90, 0, 0]
    assert _at(finger["right_forearm"]["rotation"], 0.4) == [0, 0, 0]
    assert _at(finger["right_hand"]["rotation"], 0.4) == [0, 0, 0]
    intercept = clips["animation.star_platinum.intercept"]["bones"]
    caught = clips["animation.star_platinum.intercept_catch"]["bones"]
    idle = clips["animation.star_platinum.idle"]["bones"]
    right_chain = (
        ("root", [0, 0, 0]), ("hover", [0, 0, 0]),
        ("pelvis", [0, 13, 0]), ("waist", [0, 14, 0]), ("chest", [0, 19, 0]),
        ("right_upper_arm", [-5.8, 23.4, 0]),
        ("right_forearm", [-6.2, 18.6, 0]), ("right_hand", [-6.2, 14.7, 0]),
    )
    for index in range(4, 21):
        time = index / 40
        for bone, channels in intercept.items():
            for channel, track in channels.items():
                assert _at(track, time) == _at(track, 0.1), (bone, channel, "catch window")
        wrist = _runtime_point(intercept, time, right_chain, [-6.2, 14.7, 0])
        assert -5 <= wrist[0] <= -3 and 24 <= wrist[1] <= 27 and wrist[2] < -5, wrist
    for bone in BONES:
        for channel, track in intercept[bone].items():
            assert _at(track, 0.1) == _at(caught[bone][channel], 0), (bone, "catch transition")
            assert _at(track, 0.6) == _at(idle[bone][channel], 0), (bone, "intercept ready")
            assert _at(caught[bone][channel], 0.22) == _at(idle[bone][channel], 0), (bone, "caught ready")
    # Check padded glove/head envelopes at raised keyframes, including the
    # catch twist and the lowering motion.
    head_chain = right_chain[:-3] + (("neck", [0, 25, 0]), ("head", [0, 25.1, 0]))
    glove_envelope = [[-6.2 + x, 14.7 + y, z]
                      for x in (-1.9, 1.9) for y in (-3.4, 0.1) for z in (-2.5, 1.8)]
    head_envelope = [[x, y, z]
                     for x in (-4.3, 4.3) for y in (25.4, 32.6) for z in (-4.5, 3.5)]
    clearances = []
    for name, bones in (("intercept", intercept), ("intercept_catch", caught)):
        for key in bones["right_upper_arm"]["rotation"]:
            time = float(key)
            glove = [_runtime_point(bones, time, right_chain, point) for point in glove_envelope]
            head = [_runtime_point(bones, time, head_chain, point) for point in head_envelope]
            if max(point[1] for point in glove) >= min(point[1] for point in head):
                clearance = min(point[2] for point in head) - max(point[2] for point in glove)
                assert clearance > 0, (name, time, "glove/head clearance")
                clearances.append(clearance)
    return {"clips": len(clips), "bone_tracks": track_count, "numeric_keys": key_count,
            "barrage_contacts": len(BARRAGE_CONTACTS), "star_finger_peak_seconds": 0.4,
            "star_finger_retracted_seconds": 0.7,
            "star_finger_contact_direction": [0 if abs(value) < 1e-8 else round(value, 8)
                                              for value in direction],
            "intercept_window_seconds": [0.1, 0.5],
            "intercept_wrist": [round(value, 6) for value in wrist],
            "intercept_min_head_clearance": round(min(clearances), 6)}


if __name__ == "__main__":
    print(audit_animations())
