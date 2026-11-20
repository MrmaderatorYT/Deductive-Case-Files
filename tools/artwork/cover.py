"""Draw the archive cover as an ink print on a detective's desk."""

import math
from random import Random

from vector import Art


class Plate:
    """Local drawing coordinates for rotated sheets and stationery."""

    def __init__(self, art, x, y, angle):
        self.art = art
        self.x = x
        self.y = y
        radians = math.radians(angle)
        self.cos = math.cos(radians)
        self.sin = math.sin(radians)

    def point(self, x, y):
        return (self.x + x * self.cos - y * self.sin,
                self.y + x * self.sin + y * self.cos)

    def poly(self, points, fill, stroke=None, sw=1):
        self.art.poly([self.point(x, y) for x, y in points], fill, stroke, sw)

    def rect(self, x, y, w, h, fill, stroke=None, sw=1):
        self.poly([(x, y), (x + w, y), (x + w, y + h), (x, y + h)], fill, stroke, sw)

    def circle(self, x, y, r, fill, stroke=None, sw=1):
        self.art.ellipse(*self.point(x, y), r, r, fill, stroke, sw)

    def strokes(self, lines, color, width=1):
        d = []
        for x1, y1, x2, y2 in lines:
            ax, ay = self.point(x1, y1)
            bx, by = self.point(x2, y2)
            d.append(f"M{ax:.2f},{ay:.2f}L{bx:.2f},{by:.2f}")
        self.art.path(" ".join(d), stroke=color, sw=width)


def draw():
    a = Art()
    rng = Random(27915)

    tabletop(a, rng)
    folder(a, rng)
    ledger(a, rng)
    print_photo(a, rng)
    pencil(a)
    return a


def tabletop(a, rng):
    desk = a.gradient("desk", 90, -80, 800, 620, [
        (0, "#343132"), (0.4, "#21262D"), (0.78, "#151C25"), (1, "#10151D"),
    ])
    a.rect(0, 0, 960, 540, desk)
    # Broad seams and finer broken lines follow the grain of the wood.
    a.path("M0,144C249,138 509,104 960,82 M0,367C341,365 659,312 960,306",
           stroke="#0D1119", sw=5)
    a.path("M0,148C249,142 509,109 960,87 M0,371C341,369 659,316 960,310",
           stroke="#76614A47", sw=1.5)
    for color, count in [("#8E73502B", 44), ("#050A1150", 54), ("#B29A6D16", 58)]:
        grain = []
        for _ in range(count):
            x, y = rng.randrange(-100, 940), rng.randrange(-14, 600)
            end = min(1000, x + rng.randrange(80, 437))
            span = end - x
            dy = rng.uniform(-0.1, -0.055) * span
            grain.append(
                f"M{x},{y} C{x + span * .28:.1f},{y - 1:.1f} "
                f"{x + span * .58:.1f},{y + dy * .7:.1f} {end},{y + dy:.1f}"
            )
        a.path(" ".join(grain), stroke=color, sw=1)
    a.path("M26,424C119,405 176,420 212,430C163,445 116,456 29,465"
           " M18,432C86,420 144,421 175,434C136,447 84,450 13,454"
           " M31,440C92,428 121,429 143,436C121,443 78,446 31,447",
           stroke="#A883542F", sw=2)


def folder(a, rng):
    pad = Plate(a, 121, 154, -8)
    shape = [(0, 62), (89, 62), (110, 36), (267, 36), (295, 62), (658, 62),
             (673, 76), (673, 324), (663, 336), (12, 336), (0, 324)]
    pad.poly([(x + 18, y + 25) for x, y in shape], "#080C166B")
    pad.poly([(x + 5, y + 11) for x, y in shape], "#0B111ACA")
    pad.poly([(x, y + 6) for x, y in shape], "#6D4C33", "#171822", 2)
    kraft = a.gradient("folder-copper", 134, 99, 708, 466, [
        (0, "#DEB679"), (.38, "#B48653"), (.72, "#967048"), (1, "#704C3B"),
    ])
    pad.poly(shape, kraft, "#3F312A", 2.3)
    pad.strokes([(2, 63, 89, 63), (110, 38, 264, 38), (278, 66, 659, 66),
                 (11, 332, 663, 332), (669, 78, 669, 320)], "#F1C98E92", 1.5)
    pad.strokes([(8, 303, 659, 303), (8, 306, 659, 306)], "#61453191")
    for shade in ["#50372A35", "#F3CF9A45"]:
        fibers = []
        for _ in range(88):
            x, y = rng.randrange(12, 660), rng.randrange(73, 326)
            fibers.append((x, y, x + rng.randrange(2, 12), y + rng.uniform(-1, 1)))
        pad.strokes(fibers, shade, .8)
    # Partly obscured lip of a smaller case envelope beneath the ledger.
    pad.poly([(12, 119), (300, 103), (311, 284), (18, 296)], "#342329",
             "#3D2B25", 2)
    pad.poly([(16, 120), (292, 111), (301, 274), (22, 285)], "#613A37")
    pad.poly([(16, 120), (292, 111), (159, 201)], "#895049", "#633F3A", 2)
    pad.strokes([(20, 278, 158, 201), (296, 269, 158, 201), (20, 125, 157, 208)],
                "#C48369A5", 1.15)


def ledger(a, rng):
    sheet = Plate(a, 230, 107, -10)
    sheet.rect(11, 15, 368, 294, "#382C2687")
    sheet.rect(5, 12, 370, 294, "#614431")
    sheet.rect(2, 8, 370, 294, "#8D6F4B", "#63472E", 1)
    sheet.rect(-4, 5, 372, 294, "#B2976C", "#6B5238", 1)
    paper = a.gradient("ledger-paper", 221, 100, 662, 395, [
        (0, "#E5CCA0"), (.45, "#CCAE81"), (1, "#AD8B64"),
    ])
    sheet.poly([(0, 0), (341, 0), (370, 29), (370, 294), (0, 294)], paper, "#594331", 1.3)
    sheet.poly([(341, 1), (369, 29), (342, 27)], "#F0D2A3", "#8E7151")
    sheet.rect(24, 25, 99, 8, "#6A3C34")
    sheet.rect(24, 40, 51, 2, "#6A3C34")
    sheet.strokes([(23, 64, 343, 64), (23, 201, 343, 201),
                   (23, 229, 343, 229), (23, 255, 343, 255)], "#795C43", 1.5)
    sheet.strokes([(x, 65, x, 183) for x in (24, 59, 133, 221, 298, 343)] +
                  [(24, y, 343, y) for y in range(83, 184, 20)], "#9A7B58", .8)
    for y in range(74, 176, 20):
        widths = [rng.randrange(19, 48), rng.randrange(23, 69), rng.randrange(13, 48)]
        for x, width in zip((68, 144, 230), widths):
            sheet.rect(x, y - 1, width, 2.1, "#463932C2")
            sheet.rect(x + 2, y + 2.5, width * .73, 1.1, "#6047377D")
    sheet.circle(41, 93, 4, None, "#74433C", 1.2)
    sheet.circle(43, 130, 5, None, "#74433C", 1.5)
    sheet.strokes([(41, 74, 45, 74), (42, 113, 45, 112), (40, 153, 46, 155),
                   (41, 174, 45, 175)], "#654D39", 2)
    sheet.strokes([(30, 215, 116, 215), (31, 218, 102, 218),
                   (29, 242, 100, 242), (32, 245, 119, 245),
                   (154, 215, 211, 215), (154, 219, 192, 219)], "#6D5039A6", 1.4)
    # Broken double line gives the stamped seal a worn impression.
    sheet.circle(129, 248, 29, None, "#70413988", 2)
    sheet.circle(129, 248, 24, None, "#70413971", 1.2)
    sheet.poly([(120, 237), (130, 250), (143, 235), (139, 261), (118, 260)],
               None, "#70413985", 1.7)


def print_photo(a, rng):
    p = Plate(a, 485, 119, 10)
    p.rect(13, 17, 310, 273, "#120E16A8")
    p.rect(4, 6, 309, 273, "#1B181BE6")
    stock = a.gradient("photo-stock", 485, 110, 749, 443, [
        (0, "#E2CAA0"), (.5, "#CBB18C"), (1, "#B79B7D"),
    ])
    p.rect(0, 0, 309, 273, stock, "#64503F", 1.3)
    p.rect(11, 11, 287, 235, "#504D50", "#544333")
    p.strokes([(9, 256, 300, 256), (9, 258, 300, 258)], "#EBCE9F73", .8)
    p.rect(238, 256, 36, 2, "#754D405F")
    p.rect(278, 256, 20, 2, "#754D405F")
    alley(a, p, rng)

    # The wire clip casts its own tiny shadow over the edge of the print.
    a.path("M742,143C750,144 751,148 749,155L743,182C741,189 735,190 731,187"
           "C728,185 727,181 728,177L734,153C735,149 739,148 741,151"
           "C742,153 742,155 741,157L736,180",
           stroke="#2A201DC0", sw=4)
    a.path("M741,141C749,142 750,146 748,153L742,180C740,187 734,188 730,185"
           "C727,183 726,179 727,175L733,151C734,147 738,146 740,149"
           "C741,151 741,153 740,155L735,178",
           stroke="#C49B67", sw=2.8)
    a.path("M741,141C749,142 750,146 748,153L742,180C740,187 734,188 730,185",
           stroke="#F3D2A0", sw=.8)


def alley(a, p, rng):
    mist = a.gradient("alley-mist", 612, 172, 651, 389, [
        (0, "#373A42"), (.43, "#605B58"), (.65, "#A49A81"), (1, "#292E3A"),
    ])
    p.rect(12, 12, 285, 233, mist)
    p.poly([(12, 192), (117, 150), (194, 146), (297, 185), (297, 245), (12, 245)],
           "#393942")
    p.poly([(81, 245), (149, 157), (163, 155), (238, 245)], "#625B55")
    p.poly([(111, 148), (135, 132), (137, 102), (147, 100), (147, 150)],
           "#575454")
    p.poly([(163, 151), (163, 103), (172, 93), (179, 93), (179, 151)], "#5B5554")

    # Facades converge towards the rain-lit gap in the middle.
    p.poly([(12, 12), (59, 12), (128, 82), (127, 159), (12, 218)], "#181D29")
    p.poly([(59, 12), (87, 12), (132, 87), (132, 158), (127, 159), (128, 82)],
           "#34323A")
    p.poly([(223, 12), (297, 12), (297, 216), (179, 156), (179, 88)], "#222633")
    p.poly([(212, 12), (225, 12), (188, 88), (188, 161), (179, 156), (179, 88)],
           "#3F3C42")
    p.poly([(12, 12), (22, 12), (22, 202), (12, 218)], "#141A26")
    p.poly([(265, 12), (297, 12), (297, 218), (274, 205)], "#171D2A")
    masonry(p, [(18, 16), (124, 87), (124, 155), (18, 202)], rng, "#9B86734C")
    masonry(p, [(291, 16), (190, 88), (190, 157), (291, 202)], rng, "#A8977F61")
    p.strokes([(12, y, 127, 95 + i * 9) for i, y in enumerate((39, 67, 106, 140, 176))]
              + [(188, 94 + i * 9, 297, y) for i, y in enumerate((35, 70, 113, 151, 183))],
              "#8A7D6A73", .85)
    p.strokes([(35, 12, 35, 191), (68, 28, 68, 183), (101, 62, 101, 167),
               (234, 12, 234, 185), (265, 12, 273, 204), (280, 12, 290, 208)],
              "#0D1524", 2.5)
    p.strokes([(37, 12, 37, 180), (70, 28, 70, 182), (103, 62, 103, 166),
               (235, 12, 235, 185), (268, 12, 275, 203)], "#A3937D70", .9)
    p.poly([(18, 87), (63, 106), (63, 145), (18, 141)], "#0E1625", "#544D4D")
    p.poly([(23, 92), (58, 107), (58, 139), (23, 136)], "#675552")
    p.poly([(78, 110), (94, 115), (94, 136), (78, 136)], "#756353", "#302D35")
    p.poly([(249, 78), (265, 71), (265, 106), (249, 112)], "#B18A57", "#3D3535", 2)
    p.poly([(212, 110), (225, 107), (225, 126), (212, 128)], "#AA855B", "#3D3535")
    p.strokes([(42, 98, 42, 138), (24, 113, 58, 125), (86, 114, 86, 136),
               (257, 74, 257, 110), (249, 94, 265, 90), (218, 109, 218, 126)],
              "#282A35", 1.8)
    p.poly([(27, 174), (53, 167), (53, 198), (27, 210)], "#171D26")
    p.poly([(230, 150), (256, 152), (256, 194), (230, 180)], "#111927",
           "#574D4B", 1.5)
    p.poly([(93, 143), (116, 138), (116, 161), (93, 172)], "#171F29")
    p.strokes([(71, 43, 106, 74), (106, 74, 139, 84), (139, 84, 188, 72),
               (188, 72, 245, 24), (124, 98, 146, 104), (146, 104, 172, 102),
               (172, 102, 194, 95)], "#262934", 1.2)
    p.strokes([(199, 111, 191, 108), (191, 108, 187, 114), (187, 114, 187, 121)],
              "#AE9571", 1.55)
    p.circle(187, 122, 3.1, "#DDAC68")
    p.circle(187, 123, 1.25, "#F8D591")

    for color in ("#9C8F7B73", "#262D398D", "#BDAC9156"):
        drips = []
        for _ in range(48):
            y = rng.randrange(164, 243)
            spread = 11 + (y - 161) * 1.39
            x = rng.uniform(max(13, 152 - spread), min(288, 151 + spread))
            end = min(295, x + rng.randrange(2, 10))
            drips.append((x, y, end, y - rng.uniform(0, .65)))
        p.strokes(drips, color, .85)
    p.strokes([(149, 168, 152, 171), (144, 182, 153, 185), (151, 202, 162, 204),
               (143, 218, 162, 219), (139, 232, 161, 234)], "#BEAA876E", 2.4)
    p.strokes([(24, 182, 123, 156), (15, 213, 126, 160), (185, 156, 295, 215),
               (192, 176, 262, 243), (121, 170, 59, 244)], "#A2948061", 1.1)
    # An anonymous figure and its broken reflection set the scale of the alley.
    p.circle(157, 145, 2.4, "#262A35")
    p.poly([(154, 148), (160, 148), (163, 160), (152, 161)], "#242A34")
    p.strokes([(155, 158, 154, 167), (159, 159, 161, 166),
               (156, 171, 157, 177), (160, 176, 161, 182)], "#292C37", 1.65)
    # Distress the print's edges with irregular silver flecks.
    edges = []
    for _ in range(60):
        x, y = rng.randrange(13, 291), rng.randrange(13, 243)
        if 99 < x < 207 and 24 < y < 224:
            continue
        edges.append((x, y, x + rng.uniform(.6, 2.1), y + rng.uniform(.3, 1.5)))
    p.strokes(edges, "#D2BCA65E", .9)


def masonry(p, corners, rng, tone):
    """Broken mortar lines get closer where the wall recedes."""
    outer_top, inner_top, inner_base, outer_base = corners

    def mix(start, end, ratio):
        return tuple(a + (b - a) * ratio for a, b in zip(start, end))

    strokes = []
    for course in range(1, 21):
        depth = course / 21
        near = mix(outer_top, outer_base, depth)
        far = mix(inner_top, inner_base, depth)
        for stone in range(6):
            start = (stone + (0.32 if course % 2 else 0)) / 7
            end = start + rng.uniform(.07, .135)
            px, py = mix(near, far, start)
            qx, qy = mix(near, far, end)
            strokes.append((px, py, qx, qy))
            if stone > 0:
                strokes.append((px, py, px, py + 6 * (1 - start * .62)))
    p.strokes(strokes, tone, .7)


def pencil(a):
    pen = Plate(a, 806, 285, -76)
    pen.poly([(-3, 6), (173, 6), (190, 11), (172, 16), (-3, 16)], "#10141DC4")
    pen.poly([(0, 0), (152, 0), (177, 6), (152, 12), (0, 12)], "#71443B")
    pen.rect(0, 1, 152, 2, "#BE815A")
    pen.rect(0, 4, 152, 4, "#91543E")
    pen.rect(0, 9, 152, 3, "#4F3434")
    pen.poly([(151, 0), (177, 6), (151, 12), (155, 6)], "#B29170")
    pen.poly([(169, 4), (177, 6), (169, 8)], "#23242D")
    pen.rect(8, 0, 8, 12, "#977653")
    pen.rect(10, 1, 1.5, 10, "#D1AB73")
    pen.rect(14, 1, 1, 10, "#5A413C")
