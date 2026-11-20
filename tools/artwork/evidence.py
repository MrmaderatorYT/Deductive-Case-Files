"""Draw puzzle diagrams from the exact data encoded in each case."""

import math

from vector import Art

PAPER = "#EDE1CC"
LABEL = "#F7EEDD"
RULE = "#B19C80"
INK = "#282D35"
RED = "#873C34"
BRASS = "#946136"

# Compact single-stroke lettering keeps every mark an SVG path and legible at phone sizes.
GLYPHS = {
    "0": (("M", 1.5, 0), ("Q", 0, 0, 0, 2.5), ("Q", 0, 5, 1.5, 5),
          ("Q", 3, 5, 3, 2.5), ("Q", 3, 0, 1.5, 0)),
    "1": (("M", .45, 1), ("L", 1.65, 0), ("L", 1.65, 5),
          ("M", .4, 5), ("L", 2.9, 5)),
    "2": (("M", .1, 1.05), ("Q", .6, -.15, 1.75, 0), ("Q", 3.8, .25, 2.6, 2.1),
          ("L", .13, 5), ("L", 3, 5)),
    "3": (("M", .25, .6), ("Q", 1.25, -.4, 2.42, .2), ("Q", 3.75, 1.1, 1.55, 2.35),
          ("Q", 3.2, 2.4, 3.05, 3.72), ("Q", 2.7, 5.85, .15, 4.52)),
    "4": (("M", 2.35, 5), ("L", 2.35, 0), ("L", 0, 3.35), ("L", 3.05, 3.35)),
    "5": (("M", 2.9, 0), ("L", .6, 0), ("L", .3, 2.35), ("Q", 2.3, 1.65, 2.9, 2.8),
          ("Q", 3.9, 5.75, .2, 4.55)),
    "6": (("M", 2.8, .35), ("Q", 1.25, -.7, .45, 1.15), ("Q", -.1, 2.25, .13, 3.7),
          ("Q", .46, 5.3, 2.12, 4.95), ("Q", 3.75, 4.28, 2.85, 2.8),
          ("Q", 1.9, 1.75, .2, 2.95)),
    "7": (("M", 0, 0), ("L", 3, 0), ("L", .95, 5)),
    "8": (("M", 1.5, 2.45), ("Q", -.4, 1.72, .3, .55), ("Q", 1.5, -.7, 2.7, .55),
          ("Q", 3.4, 1.72, 1.5, 2.45), ("Q", -.6, 3.2, .13, 4.4),
          ("Q", 1.5, 5.65, 2.87, 4.4), ("Q", 3.6, 3.2, 1.5, 2.45)),
    "9": (("M", .3, 4.65), ("Q", 1.95, 5.7, 2.65, 3.75), ("Q", 3.07, 2.75, 2.87, 1.25),
          ("Q", 2.6, -.35, 1, .05), ("Q", -.75, .65, .15, 2.25),
          ("Q", 1.1, 3.3, 2.8, 2.05)),
    "A": (("M", -.15, 5), ("L", 1.5, 0), ("L", 3.15, 5), ("M", .45, 3.25), ("L", 2.55, 3.25)),
    "B": (("M", .15, 2.35), ("L", 1.65, 2.35), ("Q", 3.1, 2.35, 3.1, 3.65),
          ("Q", 3.1, 5, 1.65, 5), ("L", .15, 5), ("L", .15, 0), ("L", 1.62, 0),
          ("Q", 2.9, 0, 2.9, 1.13), ("Q", 2.9, 2.35, 1.65, 2.35)),
    "C": (("M", 2.9, .55), ("Q", 2, -.32, 1.04, .25), ("Q", 0, .9, 0, 2.5),
          ("Q", 0, 4.1, 1.04, 4.75), ("Q", 2, 5.32, 2.9, 4.45)),
    "D": (("M", .08, 5), ("L", .08, 0), ("L", 1.05, 0), ("Q", 3, 0, 3, 2.5),
          ("Q", 3, 5, 1.05, 5), ("L", .08, 5)),
    "?": (("M", .1, .85), ("Q", 1.4, -.6, 2.75, .45), ("Q", 3.7, 1.65, 1.5, 2.65),
          ("L", 1.5, 3.45), ("M", 1.5, 4.98), ("L", 1.5, 5.05)),
}


def lettering(a, text, cx, cy, height=5):
    scale = height / 5
    left = cx - (len(text) * 4.3 - 1.3) * scale / 2
    top = cy - height / 2
    for index, char in enumerate(text):
        x = left + index * 4.3 * scale
        commands = []
        for command, *coordinates in GLYPHS[char]:
            points = zip(coordinates[::2], coordinates[1::2])
            commands.append(command + " ".join(f"{x + px * scale:.2f},{top + py * scale:.2f}"
                                                for px, py in points))
        a.path(" ".join(commands), stroke=INK, sw=.79 * scale)


def draw(spec):
    a = Art(160, 90)
    a.rect(0, 0, 160, 90, PAPER)
    a.rect(3, 3, 154, 84, None, RULE, .48)
    kind, *parts = spec.split(":")
    if kind == "CLOCK":
        clock(a, *map(int, parts))
    elif kind == "KEYS":
        keys(a, parts)
    elif kind == "SHELF":
        shelf(a, [int(level) for level in parts[0].split(",")])
    elif kind == "TRAIL":
        trail(a, *[[int(room) for room in path.split("-")] for path in parts])
    else:
        raise ValueError(f"Unknown visual evidence: {spec}")
    return a


def circle(a, x, y, r, fill, stroke=None, width=1):
    a.ellipse(x, y, r, r, fill, stroke, width)


def clock(a, hour, minute):
    for start, end in [(14, 36), (124, 146)]:
        a.line(start, 45, end, 45, RULE, .55)
    circle(a, 81, 46.5, 38, "#D5C0A5")
    circle(a, 80, 45, 38, INK)
    circle(a, 80, 45, 36.8, BRASS)
    circle(a, 80, 45, 35.5, LABEL)
    circle(a, 80, 45, 34.3, None, RULE, .55)
    for i in range(60):
        angle = math.tau * i / 60
        x, y = math.sin(angle), -math.cos(angle)
        major = i % 5 == 0
        inner = 29.5 if major else 32
        a.line(80 + x * inner, 45 + y * inner, 80 + x * 33, 45 + y * 33,
               INK if major else BRASS, 1.15 if major else .42)
        if i % 15 == 0:
            lettering(a, str(i // 5 or 12), 80 + x * 24, 45 + y * 24, 5.2)
    for ratio, length, weight, color in (
        (((hour % 12) + minute / 60) / 12, 18, 2.75, INK),
        (minute / 60, 27, 1.7, RED),
    ):
        angle = math.tau * ratio
        a.line(80, 45, 80 + math.sin(angle) * length, 45 - math.cos(angle) * length,
               color, weight)
    circle(a, 80, 45, 2.25, BRASS)
    circle(a, 80, 45, .85, LABEL)
    a.path("M102,17.5L108,25L105,29 M108,25L112,30", stroke=RULE, sw=.65)


def key_blade(a, pattern, x, y, color):
    top = y - 4
    step = 22 / len(pattern)
    vertices = [(x, top), (x, y)]
    for index, tooth in enumerate(pattern):
        tip = y + int(tooth) * 2.5
        vertices.extend([(x + index * step, tip), (x + (index + 1) * step, tip)])
    vertices.append((x + 22, top))
    a.poly(vertices, color)


def keys(a, patterns):
    a.rect(51, 9, 58, 24, "#D6B994", rx=2)
    a.rect(50, 7, 58, 24, "#E2C7A5", rx=2)
    key_blade(a, patterns[0], 75, 15, RED)
    a.line(75.5, 11.6, 96.5, 11.6, "#B17860", .55)
    lettering(a, "?", 61, 19, 5.75)
    for index, pattern in enumerate(patterns[1:5]):
        x = 6 + index * 37.5
        a.rect(x, 39, 35.5, 43, LABEL, RULE, .5)
        a.line(x, 70, x + 35.5, 70, RULE, .5)
        circle(a, x + 6.9, 52, 4.3, INK)
        circle(a, x + 6.9, 52, 1.75, LABEL)
        key_blade(a, pattern, x + 10.5, 54, INK)
        a.line(x + 10.5, 50.7, x + 31.7, 50.7, BRASS, .55)
        lettering(a, "ABCD"[index], x + 17.75, 76.5)


def shelf(a, levels):
    step = 140 / len(levels)
    w = min(18, step - 6)
    a.rect(7, 68, 146, 3, INK)
    a.rect(7, 69, 146, .65, BRASS)
    for index, level in enumerate(levels):
        x = 10 + index * step + (step - w) / 2
        height = level * 4
        a.rect(x + 2, 19.5, w, 48.5, "#DCC9AB", rx=2.5)
        a.rect(x, 19, w, 49, LABEL, rx=2.5)
        if height > 0:
            a.rect(x + 1.25, 66.4 - height, w - 2.5, height, BRASS, rx=1.25)
            a.line(x + 2.25, 67.15 - height, x + w - 2.25, 67.15 - height, RED, .7)
        a.rect(x, 19, w, 49, None, INK, .8, rx=2.5)
        a.rect(x + 1, 13, w - 2, 6.5, INK, rx=1)
        a.line(x + 2.2, 15, x + w - 2.2, 15, RULE, .65)
        a.line(x + 3, 23, x + 3, 64, "#DCC9AB", .65)
        lettering(a, str(index + 1), x + w / 2, 78.5)


def trail(a, round_rooms, square_rooms):
    for i in range(6):
        x = 10 + i % 3 * 51
        y = 8 + i // 3 * 44
        a.rect(x, y, 38, 30, LABEL, INK)
        a.line(x + 2, y + 2, x + 36, y + 2, RULE, .5)
        lettering(a, str(i + 1), x + 7, y + 7)
    route(a, round_rooms, True, -3.4)
    route(a, square_rooms, False, 3.4)


def route(a, rooms, rounded, lane):
    def mark(x, y):
        if rounded:
            circle(a, x, y, 1.28, INK)
        else:
            a.rect(x - 1.18, y - 1.18, 2.36, 2.36, RED)

    def track(x0, y0, x1, y1):
        count = max(1, math.floor((abs(x1 - x0) + abs(y1 - y0)) / 5 + .5))
        for i in range(1, count + 1):
            mark(x0 + (x1 - x0) * i / count, y0 + (y1 - y0) * i / count)

    corridor = 45 + lane
    previous = None
    for room in rooms:
        row, column = divmod(room - 1, 3)
        x, y = 29 + column * 51 + lane, 23 + row * 44 + lane
        if previous is None:
            mark(x, y)
        else:
            old_row, old_column, px, py = previous
            if column == old_column or (row == old_row and abs(column - old_column) == 1):
                track(px, py, x, y)
            else:
                # Stay in the corridor when the next room is not adjacent.
                track(px, py, px, corridor)
                track(px, corridor, x, corridor)
                track(x, corridor, x, y)
        previous = row, column, x, y
