"""Original full-body poses, eased at 40 Hz; impacts retain their server timestamps."""
from bisect import bisect_right
from copy import deepcopy
from math import sin, pi


def pose(base, rotation=None, position=None, scale=None):
    result = deepcopy(base)
    for channel, values in [('rotation', rotation), ('position', position), ('scale', scale)]:
        for bone, vector in (values or {}).items():
            result[bone][channel] = vector
    return result


def bake(length, frames, loop=False):
    assert frames[0][0] == 0 and frames[-1][0] == length
    authored = [t for t, _ in frames]
    assert all(a < b for a, b in zip(authored, authored[1:]))
    times = sorted(set(authored + [round(i / 40, 5) for i in range(int(length * 40) + 1)]))
    tracks = {b: {c: {} for c in data} for b, data in frames[0][1].items()}
    for t in times:
        segment = min(bisect_right(authored, t) - 1, len(frames) - 2)
        start, first = frames[segment]
        end, last = frames[segment + 1]
        u = (t - start) / (end - start)
        u = u * u * (3 - 2 * u)
        for b, channels in tracks.items():
            for c, values in channels.items():
                vector = [round(a + (z - a) * u, 5) for a, z in zip(first[b][c], last[b][c])]
                values[f'{t:g}'] = [vector[0], -vector[1], -vector[2]] if c == 'rotation' else vector
    for channels in tracks.values():
        for c, values in channels.items():
            if all(v == next(iter(values.values())) for v in values.values()): channels[c] = next(iter(values.values()))
    return {'animation_length': length, 'loop': loop, 'bones': tracks}


def ready(kind, rig):
    value = {b['name']: {'rotation': [0, 0, 0]} for b in rig if not b['name'].startswith('armor_')}
    value['root'].update(position=[0, .4, 0], scale=[1, 1, 1])
    if kind == 'the_fool':
        return pose(value, {'head': [2, -3, 0], 'torso': [-2, 0, 0]})
    rotations = {'body': [0, -3, 2], 'torso': [-3, 8, -2], 'head': [1, -5, 0],
                 'right_arm': [-15, 9, -12], 'right_forearm': [-43, 0, 0], 'right_hand': [4, 0, 0],
                 'left_arm': [-24, -10, 13], 'left_forearm': [-48, 0, 0], 'left_hand': [5, 0, 0],
                 'right_leg': [10, 4, -5], 'right_shin': [16, 0, 0],
                 'left_leg': [-11, -4, 5], 'left_shin': [21, 0, 0]}
    if kind == 'silver_chariot':
        rotations.update(right_arm=[-5, 0, -8], right_forearm=[-8, 0, 0], right_hand=[0, 0, 0],
                         left_arm=[-20, -15, 22], left_forearm=[-38, 0, 0], body=[0, -8, 1], torso=[0, 15, -2])
        value['rapier']['position'] = [0, 0, 0]
    if kind in ['magicians_red', 'hierophant_green']:
        rotations.update(right_arm=[-19, 8, -17], left_arm=[-24, -9, 20],
                         right_forearm=[-26, 0, 0], left_forearm=[-32, 0, 0])
    return pose(value, rotations)


def locomotion(kind, rig):
    base = ready(kind, rig)
    inhale = pose(base, {'torso': [-5, 9, -2.5], 'head': [2, -5, 1],
                         'right_forearm': [-45, 0, 0], 'left_forearm': [-50, 0, 0]},
                  {'root': [0, .95, 0]}) if kind != 'the_fool' else pose(base, {'head': [0, 3, 1]}, {'root': [0, .62, 0]})
    if kind == 'silver_chariot': inhale = pose(base, {'torso': [-1.5, 16, -2], 'head': [1, -6, 0]}, {'root': [0, .9, 0]})
    idle = bake(2.4, [(0, base), (.9, inhale), (1.3, pose(inhale, {'head': [2, -3, -.5]})), (2.4, base)], True)
    frames = []
    for i in range(9):
        phase = i / 8 * 2 * pi
        if kind == 'the_fool':
            rotations = {'body': [sin(phase) * 3, 0, sin(phase) * 2], 'head': [-sin(phase) * 3, 0, 0]}
            for side, sign in [('left', 1), ('right', -1)]:
                rotations[side + '_arm'] = [sin(phase) * 19 * sign, 0, 0]
                rotations[side + '_leg'] = [-sin(phase) * 19 * sign, 0, 0]
            value = pose(base, rotations, {'root': [0, .45 + sin(phase * 2) * .2, 0]})
        else:
            rotations = {'body': [3, -3, 2], 'torso': [6, 7 + sin(phase) * 2, -2], 'head': [-5, -5, 0],
                         'right_leg': [23 + sin(phase) * 5, 4, -6], 'right_shin': [30 - sin(phase) * 4, 0, 0],
                         'left_leg': [8 - sin(phase) * 5, -4, 5], 'left_shin': [33 + sin(phase) * 4, 0, 0]}
            if kind != 'silver_chariot':
                rotations.update(right_arm=[-6, 10, -17], left_arm=[-11, -12, 19],
                                 right_forearm=[-42 + sin(phase + .5) * 4, 0, 0], left_forearm=[-50 - sin(phase + .5) * 4, 0, 0])
            value = pose(base, rotations, {'root': [0, .9 + sin(phase) * .3, 0]})
        frames.append((round(i * .15, 3), value))
    frames[-1] = (1.2, deepcopy(frames[0][1]))
    return {'idle': idle, 'move': bake(1.2, frames, True)}


def clips(kind, rig, original):
    base = ready(kind, rig)
    data = locomotion(kind, rig)
    contact = .15 if kind == 'silver_chariot' else .2 if kind == 'the_world' else .3
    if kind == 'the_fool':
        wind = pose(base, {'body': [-8, 0, 0], 'head': [-12, 0, 0]}, {'root': [0, -.25, .3]})
        hit = pose(base, {'body': [7, 0, 0], 'head': [12, 0, 0]}, {'root': [0, .8, -1.1]})
        data['attack'] = bake(1.0, [(0, base), (.19, wind), (.3, hit), (.4, pose(hit, {'head': [7, 0, 0]})), (.72, pose(base, {'torso': [2, 2, 0]})), (1.0, base)])
    else:
        wind = pose(base, {'body': [0, -10, 2], 'torso': [1, -20, -4],
                           'right_arm': [-25, 20, -24], 'right_forearm': [-75, 0, 0]}, {'root': [0, .3, .35]})
        hit = pose(base, {'body': [2, 8, 1], 'torso': [-5, 22, 3], 'head': [3, -14, -2],
                          'right_arm': [-91, 0, -6], 'right_forearm': [-3, 0, 0], 'right_hand': [-5, 0, 0],
                          'left_arm': [-27, -10, 18], 'left_forearm': [-63, 0, 0],
                          'right_leg': [15, 4, -6], 'left_leg': [-19, -4, 6]}, {'root': [0, .4, -1.5]})
        if kind == 'silver_chariot':
            wind = pose(base, {'torso': [0, -12, -3], 'right_arm': [-9, 8, -12], 'right_forearm': [-18, 0, 0]}, {'rapier': [0, 0, .7], 'root': [0, .35, .4]})
            hit = pose(base, {'body': [1, 4, 1], 'torso': [-3, 19, 1], 'right_arm': [0, 0, -5],
                              'right_forearm': [0, 0, 0], 'left_arm': [-26, -15, 30], 'left_leg': [-21, -4, 5]},
                       {'rapier': [0, 0, -1.5], 'root': [0, .4, -1.3]})
        if kind == 'magicians_red':
            wind = pose(wind, {'left_arm': [-38, -20, 24], 'left_forearm': [-59, 0, 0], 'torso': [4, -5, 0]})
            hit = pose(hit, {'torso': [-7, 5, 0], 'left_arm': [-86, -4, 13], 'left_forearm': [-7, 0, 0]})
        data['attack'] = bake(1.0, [(0, base), (round(contact * .62, 4), wind), (contact, hit),
                                 (round(contact + .07, 4), pose(hit, {'torso': [-2, 12, 0]})), (.72, pose(base, {'torso': [2, 2, 0]})), (1.0, base)])
    if kind in ['silver_chariot', 'the_world']:
        frames = [(0, base)]
        for i in range(10 if kind == 'silver_chariot' else 12):
            t = round(.15 + i * .15, 4)
            if kind == 'silver_chariot':
                load = pose(base, {'torso': [0, 6, -2]}, {'rapier': [0, 0, .8]})
                strike = pose(base, {'torso': [-2, 15, 1], 'right_forearm': [-2, 0, 0]}, {'rapier': [0, 0, -1.7], 'root': [0, .55, -.5]})
            else:
                side, opposite = ('right', 'left') if i % 2 == 0 else ('left', 'right')
                sign = 1 if side == 'right' else -1
                load = pose(base, {side + '_arm': [-36, sign * 15, -sign * 15], side + '_forearm': [-62, 0, 0], 'torso': [-2, -sign * 11, -sign * 2]})
                strike = pose(base, {side + '_arm': [-91, 0, -sign * 6], side + '_forearm': [-3, 0, 0],
                                     opposite + '_arm': [-37, 0, sign * 17], opposite + '_forearm': [-58, 0, 0],
                                     'torso': [-4, sign * 16, sign * 3], 'head': [2, -sign * 10, 0]}, {'root': [0, .5, -.7]})
            frames.extend([(round(t - .045, 4), load), (t, strike)])
        length = 1.85 if kind == 'silver_chariot' else 2.2
        frames.append((length, base))
        data['secondary'] = bake(length, frames)
    # Add eased shoulders, elbows, waist and follow-through to long support poses.
    for name in ['secondary', 'utility']:
        if name in data: continue
        length = original['animations'][f'animation.{kind}.{name}']['animation_length']
        rotations = {'torso': [-5, 0, 0], 'head': [4, 0, 0], 'right_arm': [-58, -22, -26],
                     'left_arm': [-58, 22, 26], 'right_forearm': [-47, 0, 0], 'left_forearm': [-47, 0, 0]}
        if kind == 'the_fool': rotations = {'torso': [-12, 0, 0], 'head': [8, 0, 0], 'left_arm': [-18, 0, 15], 'right_arm': [-18, 0, -15]}
        if kind == 'silver_chariot': rotations.update(right_arm=[-28, 0, -48], left_arm=[-28, 0, 48], torso=[-8, 0, 0])
        held = pose(base, rotations, {'root': [0, .75, 0]})
        breathe = pose(held, {'head': [2, -2, 1]}, {'root': [0, 1, 0]})
        data[name] = bake(length, [(0, base), (min(.2, length * .3), held), (length * .6, breathe), (length - .1, held), (length, base)])
    data['summon'] = bake(.55, [(0, pose(base, position={'root': [0, -2, .6]}, scale={'root': [.02] * 3})), (.38, pose(base, position={'root': [0, .7, -.15]}, scale={'root': [1.04] * 3})), (.55, base)])
    data['dismiss'] = bake(.2, [(0, base), (.2, pose(base, scale={'root': [.01] * 3}))])
    data['recover'] = bake(.2, [(0, pose(base, {'torso': [2, 4, 1]})), (.2, base)])
    expanded = pose(base, {'torso': [-12, 0, 0], 'head': [7, 0, 0], 'right_arm': [-48, -15, -75],
                           'left_arm': [-48, 15, 75], 'right_forearm': [-16, 0, 0], 'left_forearm': [-16, 0, 0]}, {'root': [0, 1, 0]})
    data['time_stop'] = bake(.7, [(0, base), (.13, expanded), (.36, pose(expanded, {'head': [5, 0, 0]})), (.7, base)])
    for name, clip in data.items(): original['animations'][f'animation.{kind}.{name}'] = clip
    return original


def hierophant(original, rig):
    kind = 'hierophant_green'
    original['animations'].update({f'animation.{kind}.{k}': v for k, v in locomotion(kind, rig).items()})
    base = ready(kind, rig)
    charge = pose(base, {'torso': [5, -5, 0], 'left_arm': [-47, -20, 23], 'right_arm': [-47, 20, -23],
                         'left_forearm': [-49, 0, 0], 'right_forearm': [-49, 0, 0]}, {'root': [0, .2, .6]})
    release = pose(base, {'torso': [-6, 4, 0], 'left_arm': [-84, -9, 12], 'right_arm': [-84, 9, -12],
                          'left_forearm': [-6, 0, 0], 'right_forearm': [-6, 0, 0],
                          'left_hand': [-14, 7, 0], 'right_hand': [-14, -7, 0]}, {'root': [0, .6, -.8]})
    recoil = pose(release, {'torso': [-2, 2, 0], 'left_forearm': [-14, 0, 0], 'right_forearm': [-14, 0, 0]}, {'root': [0, .6, -.4]})
    original['animations'][f'animation.{kind}.splash'] = bake(1.6, [(0, base), (.2, charge), (.3, release), (.4, recoil), (.5, release), (.68, recoil), (1.05, pose(base, {'torso': [3, 1, 0]})), (1.6, base)])
    for name in ['summon', 'dismiss', 'recover']:
        length = original['animations'][f'animation.{kind}.{name}']['animation_length']
        start, end = base, base
        if name == 'summon': start = pose(base, scale={'root': [.02] * 3}, position={'root': [0, -2, 0]})
        if name == 'dismiss': end = pose(base, scale={'root': [.01] * 3})
        original['animations'][f'animation.{kind}.{name}'] = bake(length, [(0, start), (length, end)])
    return original
