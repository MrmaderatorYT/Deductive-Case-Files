"""Manor, café, night express and library woodcuts."""

from random import Random

from vector import Art, number


def scratches(a, seed, bounds, count, ink, slope=2):
    rng = Random(seed)
    x0, y0, x1, y1 = bounds
    fragments = []
    for _ in range(count):
        x, y = rng.uniform(x0, x1), rng.uniform(y0, y1)
        size = rng.uniform(2, 20)
        fragments.append(f"M{number(x)} {number(y)}l{number(size)} {number(slope * rng.random())}")
    a.path(" ".join(fragments), stroke=ink, sw=0.9)


def manor():
    a = Art()
    sky = a.gradient("sky", 0, 0, 0, 390, [(0, "#28323B"), (0.66, "#555B5B"), (1, "#9C8469")])
    a.rect(0, 0, 960, 540, sky)
    a.ellipse(728, 120, 49, 49, "#CFB58B55")
    scratches(a, 16, (0, 45, 945, 320), 420, "#E1CAA528", -1)
    a.path("M0 317Q182 273 263 316Q429 276 504 315Q731 260 960 330V540H0Z", "#30312F")
    a.path("M0 418Q214 385 331 411L642 405Q817 385 960 424V540H0Z", "#24282C")
    a.poly([(432, 395), (525, 395), (671, 540), (249, 540)], "#695C4D")
    scratches(a, 21, (0, 433, 940, 537), 430, "#B5915E36", -2)
    for offset in (-56, -30, -6, 17, 41, 59):
        a.path(f"M{480 + offset} 407Q{480 + offset * 1.7} 467 {479 + offset * 2.75} 540",
               stroke="#22252B85", sw=1.5)
    for x in (207, 595):
        a.rect(x, 227, 157, 172, "#3F3F3D", "#242930", 3)
        a.poly([(x - 17, 228), (x + 64, 166), (x + 132, 173), (x + 167, 227)], "#282A2E", "#887764", 2)
        for roof_y in range(191, 224, 7):
            inset = (228 - roof_y) * 0.85
            a.line(x + inset - 8, roof_y, x + 150 - inset / 2, roof_y, "#5B5048", 1.5)
        for y in range(244, 391, 12):
            a.line(x + 3, y, x + 154, y, "#82725B54", 1)
    a.rect(334, 195, 276, 209, "#474642", "#20242A", 4)
    a.poly([(306, 203), (481, 99), (636, 203)], "#28282B", "#A48D6D", 3)
    a.poly([(373, 202), (479, 116), (574, 202)], "#554A43", "#A48D6D", 2)
    a.rect(372, 201, 207, 191, "#535048", "#2A2C2E", 3)
    for x in (348, 596):
        a.rect(x, 143, 18, 73, "#49433F", "#20242A", 2)
        a.rect(x - 3, 143, 24, 8, "#8A7864", "#2A2D2F", 2)
    for y in range(207, 390, 12):
        a.line(338, y, 606, y, "#AE997054", 1.3)
        for x in range(340 + y % 21, 606, 25):
            a.line(x, y - 7, x, y, "#2F3030", 1.1)
    a.ellipse(479, 173, 20, 21, "#25262A", "#B79B73", 3)
    a.ellipse(479, 173, 13, 14, "#BA8F59")
    a.line(466, 173, 492, 173, "#374041", 2)
    a.line(479, 159, 479, 186, "#374041", 2)
    for x in (236, 294, 391, 451, 511, 629, 689):
        for y in (245, 321):
            if y == 321 and x in (451, 511):
                continue
            a.path(f"M{x} {y+46}V{y+13}Q{x+12} {y-3} {x+27} {y+13}V{y+46}Z",
                   "#252829", "#968369", 2.5)
            a.path(f"M{x+5} {y+40}V{y+16}Q{x+12} {y+6} {x+22} {y+16}V{y+40}Z", "#B78F5F")
            a.line(x + 13, y + 11, x + 13, y + 41, "#413732", 2)
            a.line(x + 5, y + 26, x + 22, y + 26, "#413732", 2)
            a.line(x - 4, y + 46, x + 31, y + 46, "#A7947A", 2)
    a.path("M432 405V341Q432 308 478 294Q524 308 524 341V405Z", "#242629", "#B69667", 3)
    a.path("M445 404V345Q445 324 478 315Q511 324 511 345V404Z", "#73543C")
    a.path("M449 346Q459 328 478 324Q499 329 507 346Z", "#CE9E61")
    a.line(478, 338, 478, 404, "#33292A", 2.2)
    for y in range(397, 423, 6):
        a.line(408 - (y - 391) * 0.6, y, 548 + (y - 391) * 0.6, y, "#B2A083", 2)
    # Black branches frame the view and break the rigid geometry of the facade.
    a.path("M0 535Q31 403 48 284Q64 192 30 117Q15 81 0 70V0H23Q70 75 76 147Q108 111 135 29L148 0H162Q150 87 92 163Q82 204 88 237Q138 213 190 147L203 129L198 155Q156 224 89 262Q77 394 56 540Z", "#191F27")
    a.path("M960 509Q930 347 905 238Q875 205 820 177L787 144L805 149L901 212Q892 159 897 120Q866 93 852 50L858 48Q878 84 902 96Q915 37 946 0H960Q920 69 922 139Q938 112 960 101V124Q923 157 930 204L960 330Z", "#191F27")
    a.path("M43 133L34 85L7 47M98 150L132 126L157 80M136 222L158 208L178 175M874 185L838 196L818 185M904 203L872 152L862 120",
           stroke="#191F27", sw=4)
    return a


def cafe():
    a = Art()
    a.rect(0, 0, 960, 540, "#282832")
    a.poly([(0, 0), (771, 0), (960, 124), (960, 379), (763, 427), (0, 448)], "#5C3437", "#211F28", 5)
    a.poly([(760, 0), (960, 124), (960, 379), (760, 428)], "#35303A")
    a.poly([(0, 109), (750, 116), (960, 213), (960, 233), (745, 154), (0, 149)], "#231F25", "#A67152", 2.6)
    for y in range(30, 108, 10):
        a.line(0, y, 765, y + 9, "#976F5B49", 1)
    reflection = a.gradient("glass", 0, 180, 0, 416, [(0, "#6F5148"), (0.48, "#CE9862"), (1, "#503D3D")])
    for left, width in ((78, 197), (286, 197), (494, 197)):
        a.rect(left, 162, width, 242, "#27262C", "#9F6B52", 4)
        a.rect(left + 9, 173, width - 18, 219, reflection, "#B3835B", 2)
        for y in (236, 258):
            a.line(left + 12, y, left + width - 13, y, "#5C4037", 4)
        for i in range(9):
            x = left + 18 + i * 19
            top = 210 + (i * 13 % 18)
            a.path(f"M{x} 234V{top+5}h3v-6h4v6h3v{234-top-5}Z", "#534148")
        a.path(f"M{left+18} 365V332q0 -24 20 -24t19 24v29M{left+140} 367V331q0 -24 16 -24t17 24v31",
               stroke="#4B3534", sw=5)
        a.ellipse(left + 107, 322, 62, 8, "#473234")
        a.line(left + 107, 322, left + 107, 382, "#362D2E", 5)
        a.path(f"M{left+75} 389l32 -12l31 12", stroke="#392F32", sw=4)
        a.line(left + 95, 174, left + 95, 393, "#302932", 5)
        a.line(left + 104, 174, left + 104, 393, "#B2815C", 1)
        a.path(f"M{left+3} 292h191", stroke="#A97855", sw=4)
        a.path(f"M{left+139} 174v20l-12 11h27l-13 -11v-20", "#F1C891")
        a.path(f"M{left+14} 187l73 17m20 12l76 20M{left+20} 208l48 13m64 23l42 12",
               stroke="#F5D9AA60", sw=1.7)
    a.poly([(777, 210), (870, 252), (870, 393), (777, 413)], "#B38460", "#25262E", 6)
    a.poly([(790, 228), (855, 256), (855, 374), (790, 387)], "#7C5E4C", "#443333", 3)
    a.ellipse(842, 322, 5, 7, "#EAC388")
    a.line(823, 242, 823, 382, "#422A31", 3)
    a.path("M0 435L755 424 960 380V540H0Z", "#2E2B32")
    a.path("M349 446C473 429 524 451 616 445S807 417 960 405V436Q725 483 554 467T155 484Z", "#A16F585D")
    a.path("M210 488Q547 470 687 470T960 437M30 523Q510 473 557 491T960 456",
           stroke="#D5A77770", sw=2)
    for y in (450, 476, 511, 537):
        a.line(0, y, 960, y - 55, "#A3958052", 2)
    scratches(a, 13, (7, 438, 940, 540), 450, "#C4896856", -1)
    rng = Random(509)
    rain = []
    for _ in range(320):
        x, y = rng.randrange(960), rng.randrange(540)
        height = rng.randrange(4, 15)
        rain.append(f"M{x} {y}l-2 {height}")
    a.path(" ".join(rain), stroke="#C9CBC343", sw=0.8)
    return a


def train():
    a = Art()
    sky = a.gradient("station", 0, 0, 0, 540, [(0, "#18232B"), (0.57, "#4F5753"), (1, "#866E52")])
    a.rect(0, 0, 960, 540, sky)
    vp = (277, 272)
    for x in (-530, -300, -105, 89, 287, 490, 705, 923, 1180):
        a.line(*vp, x, 0, "#23282D", 7)
        a.line(*vp, x - 7, 0, "#8F806966", 2)
    for i in (1.0, 0.73, 0.52, 0.35, 0.23, 0.14, 0.08):
        def x(n):
            return number(vp[0] + (n - vp[0]) * i)
        def y(n):
            return number(vp[1] + (n - vp[1]) * i)
        curve = f"M{x(-120)} {y(303)}Q{x(136)} {y(-166)} {x(519)} {y(-74)}Q{x(835)} {y(-14)} {x(1130)} {y(328)}"
        a.path(curve, stroke="#22292F", sw=13 * i)
        a.path(curve, stroke="#8A765A", sw=4 * i)
    a.path("M0 385L277 281L960 455V540H0Z", "#59534C")
    a.poly([(114, 540), (278, 280), (323, 278), (849, 540)], "#3A3737")
    for x in (255, 423, 588, 789):
        a.line(281, 284, x, 540, "#AEA28A", 3.4)
    for i in range(29):
        scaled = (i / 28) ** 2
        y = 286 + scaled * 250
        a.line(279 + scaled * 60, y, 279 + scaled * 532, y + scaled * 8, "#24272B", 2 + scaled * 3)
    for x, y, w in ((45, 69, 32), (150, 143, 21), (206, 193, 12), (233, 214, 8)):
        a.poly([(x, y), (x + w, y - 9), (x + w, 444 - x * 0.5), (x, 460 - x * 0.5)], "#33343A", "#9A7758", 2)
        a.ellipse(x + w + 7, y + 11, w * 0.43, w * 0.32, "#E7BD81", "#654D41", 2)
    a.path("M283 269L508 197Q555 128 647 133Q772 132 810 223L443 275 286 293Z", "#30363B", "#8D7862", 3)
    a.poly([(286, 275), (519, 216), (545, 463), (286, 314)], "#5A3B39", "#24292E", 4)
    for k in range(16):
        p = (k / 16) ** 2
        x = 301 + p * 211
        y = 269 - p * 42
        w = 4 + p * 22
        h = 10 + p * 54
        a.poly([(x, y), (x + w, y - 4), (x + w, y + h), (x, y + h * 0.78)], "#C09563", "#332E32", 2.4)
        a.line(x, y + h * 0.45, x + w, y + h * 0.52, "#DBC297", 1)
    for y in range(325, 451, 13):
        a.line(285, 309, 548, y, "#8F6E53", 1.9)
    face = a.gradient("body", 520, 220, 803, 475, [(0, "#A18465"), (0.22, "#504745"), (0.75, "#663F3C"), (1, "#2A2A30")])
    a.path("M512 440V246Q512 155 648 145Q776 152 799 242V428Q791 463 773 477L552 483Q522 471 512 440Z",
           face, "#28272C", 6)
    a.path("M522 259Q529 193 575 178Q636 152 699 169Q767 184 788 244M522 388Q650 400 787 383M522 434Q646 454 790 427",
           stroke="#AE9270", sw=4)
    for x in (534, 664):
        a.path(f"M{x} 233Q{x+53} 219 {x+113} 232V307Q{x+53} 320 {x} 307Z", "#302E32", "#BB9C75", 4)
        a.path(f"M{x+10} 239Q{x+55} 229 {x+104} 239V295Q{x+52} 305 {x+10} 296Z", "#C09567")
        a.path(f"M{x+17} 247l42 3l24 36l-18 4l-24 -35l-22 -2Z", "#F3D5A080")
        a.line(x + 56, 233, x + 56, 302, "#2D2B30", 4)
    for x, y, size in ((648, 191, 24), (548, 392, 16), (764, 389, 16)):
        a.ellipse(x, y, size + 5, size + 5, "#302B2C", "#BC9C70", 3)
        a.ellipse(x, y, size - 3, size - 3, "#F1CF91", "#BD986E", 2)
        a.line(x - size * 0.5, y + size * 0.25, x + size * 0.5, y + size * 0.25, "#B88D59", 1)
    scratches(a, 22, (0, 4, 940, 540), 530, "#E2BB8260", -1)
    return a


def library():
    a = Art()
    wall = a.gradient("wall", 0, 65, 0, 530, [(0, "#212829"), (0.6, "#8E7454"), (1, "#372B2B")])
    a.rect(0, 0, 960, 540, wall)
    a.path("M306 363V170Q306 59 475 36Q636 56 648 170V369Z", "#393535", "#8C7759", 4)
    a.path("M406 333V173Q406 130 476 98Q544 135 544 173V333Z", "#6D6D5E", "#C6A477", 3)
    a.path("M475 102V332M476 146Q445 150 409 181M476 146Q517 151 542 181M408 204H543M408 239H543M408 275H543",
           stroke="#302D30", sw=4)
    a.path("M0 538L413 314H541L960 538Z", "#483A32", "#2C2A2C", 3)
    a.poly([(263, 540), (454, 314), (513, 314), (695, 540)], "#6F453D", "#AF8762", 4)
    for x in (50, 126, 215, 300, 376, 593, 676, 765, 844, 926):
        a.line(476, 319, x, 540, "#AE8B654E", 1.4)
    for y in range(360, 539, 20):
        factor = (y - 314) / 226
        a.line(480 - 205 * factor, y, 480 + 206 * factor, y, "#B38C606A", 1.7)
    for right in (False, True):
        def pos(x, y, z):
            scale = 1 - z
            px = 476 + (x - 476) * scale
            return (960 - px if right else px, 269 + (y - 269) * scale)
        def plane(points, fill, stroke=None, sw=1):
            a.poly([pos(*point) for point in points], fill, stroke, sw)
        plane([(0, -167, 0), (0, 540, 0), (0, 540, 0.86), (0, -167, 0.86)], "#392D2B", "#9A7250", 4)
        rng = Random(912 if right else 1610)
        for y in (56, 169, 279, 383):
            row = 0.015
            while row < 0.849:
                z = min(row + rng.uniform(0.01, 0.029), 0.86)
                h = rng.randint(58, 99)
                color = rng.choice(("#53463C", "#826148", "#724C42", "#64413E", "#777257", "#3E4848", "#AA885F"))
                plane([(0, y + 101 - h, row), (0, y + 101 - h, z),
                       (0, y + 101, z), (0, y + 101, row)], color, "#2D292A", 1.1)
                if row < 0.55:
                    a.line(*pos(0, y + 118 - h, row + 0.003),
                           *pos(0, y + 118 - h, z - 0.002), "#CCA67488", 1.5)
                row = z + 0.002
            plane([(0, y + 102, 0), (0, y + 102, 0.86), (0, y + 113, 0.86), (0, y + 113, 0)],
                  "#AF8257", "#352B2A", 2.5)
        for z in (0.025, 0.275, 0.48, 0.64, 0.79, 0.86):
            plane([(0, -164, z), (18, -164, z), (18, 541, z), (0, 541, z)], "#5C4133", "#B38A63", 2.3)
            a.line(*pos(14, -164, z), *pos(14, 541, z), "#302829", 2)
    a.poly([(421, 352), (531, 352), (554, 367), (401, 367)], "#A2784B", "#3B3030", 3)
    a.line(417, 368, 414, 399, "#43302C", 5)
    a.line(537, 368, 543, 399, "#43302C", 5)
    a.ellipse(481, 354, 15, 3, "#CC9C5E", "#463632", 1.4)
    a.path("M480 351q22 -16 6 -32", stroke="#B38652", sw=3.2)
    a.path("M468 320Q483 301 499 320Z", "#DBAA6D", "#3C3835", 2)
    a.path("M449 327L419 352H541L498 326Z", "#F6C68036")
    scratches(a, 21, (0, 1, 945, 537), 420, "#CAB59033", 1)
    return a


SCENES = {"MANOR": manor, "CAFE": cafe, "TRAIN": train, "LIBRARY": library}


def draw(scene):
    return SCENES[scene]()
