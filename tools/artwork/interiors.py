"""Engraved interiors for the case covers."""

from vector import Art


INK = "#161A21"
BRONZE = "#80603D"
GILT = "#B98A50"
PAPER = "#EBCD97"


def _hotel(a):
    wall = a.gradient("hall_wall", 365, 0, 592, 420, [
        (0, "#312833"), (0.56, "#745346"), (1, "#49363A"),
    ])
    floor = a.gradient("hall_floor", 0, 372, 0, 540, [(0, "#524345"), (1, INK)])
    left_wall = a.gradient("hall_left", 35, 30, 360, 350, [
        (0, "#292632"), (1, "#573C3E"),
    ])
    right_wall = a.gradient("hall_right", 900, 65, 625, 355, [
        (0, INK), (1, "#513C3C"),
    ])

    a.rect(0, 0, 960, 540, INK)
    a.poly([(0, 0), (960, 0), (627, 124), (337, 124)], "#352D35")
    a.poly([(0, 0), (337, 124), (337, 384), (0, 540)], left_wall)
    a.poly([(960, 0), (627, 124), (627, 384), (960, 540)], right_wall)
    a.rect(337, 124, 290, 260, wall)
    a.poly([(0, 540), (337, 384), (627, 384), (960, 540)], floor)
    a.path("M0 22L337 129H627L960 22M0 42L337 137H627L960 42",
           stroke="#8E694B", sw=2)
    a.path("M0 14L337 124H627L960 14M0 528L337 377H627L960 528",
           stroke="#1E1D25", sw=10)
    a.path("M0 511L337 371H627L960 511", stroke="#A67A53", sw=2)
    a.path("M0 356L337 293H627L960 356M0 369L337 300H627L960 369",
           stroke="#B1875E60", sw=2)

    # Receding door jambs drawn on the side walls.
    for outer, inner, top0, top1, base0, base1 in [
        (64, 151, 64, 92, 499, 464), (199, 252, 107, 124, 444, 421),
        (277, 312, 132, 143, 410, 395), (896, 809, 64, 92, 499, 464),
        (761, 708, 107, 124, 444, 421), (683, 648, 132, 143, 410, 395),
    ]:
        a.poly([(outer, top0), (inner, top1), (inner, base1), (outer, base0)],
               "#30262F", "#996D48", 3)
        a.poly([(outer + (inner - outer) * 0.14, top0 + 14),
                (inner - (inner - outer) * 0.11, top1 + 12),
                (inner - (inner - outer) * 0.11, base1 - 9),
                (outer + (inner - outer) * 0.14, base0 - 13)],
               "#1C1B24", "#644846", 2)
        a.line(outer, top0 + 6, inner, top1 + 6, "#D29D6055")
        a.line(outer, base0 - 5, inner, base1 - 5, "#D29D6055")
        a.ellipse(inner - (inner - outer) * 0.2, (top1 + base1) * 0.57, 2.8, 3.2, "#C09258")
    for offset in range(11, 40, 5):
        a.line(0, 49 + offset, 330, 142 + offset * 0.45, "#DCAC6528")
        a.line(960, 49 + offset, 634, 142 + offset * 0.45, "#DCAC651D")

    # End doorway, with a gilded transom and a narrow line of light at the threshold.
    spill = a.gradient("hall_spill", 470, 384, 470, 540, [
        (0, "#DABE8470"), (1, "#DABE8400"),
    ])
    a.poly([(445, 382), (548, 382), (719, 540), (230, 540)], spill)
    a.path("M381 383V228Q381 150 480 150Q579 150 579 228V383Z",
           "#2C2730", "#AE8158", 4)
    a.path("M398 382V234Q398 169 480 169Q562 169 562 234V382Z",
           "#201E25", "#654A3D", 4)
    door = a.gradient("hall_door", 423, 185, 552, 384, [
        (0, "#795347"), (0.54, "#50393A"), (1, "#372B32"),
    ])
    a.path("M406 380V235Q406 178 480 178Q554 178 554 235V380Z", door)
    a.path("M406 236H554M414 231H546M480 179V230M447 185L461 230M513 185L499 230",
           stroke="#C9A16C", sw=1.5)
    for x in (418, 486):
        for y, h in ((245, 67), (324, 43)):
            a.rect(x, y, 56, h, "#4A3437", "#9A714D", 1.5)
            a.rect(x + 5, y + 5, 46, h - 10, "#553C3C", "#6F5142")
            for line in range(9, 50, 8):
                a.line(x + line, y + 9, x + line, y + h - 9, "#C5946131")
    a.rect(461, 260, 39, 23, "#C49658", "#3D2C2A", 2, 2)
    a.poly([(470, 272), (480, 265), (490, 272), (480, 278)], "#77533A")
    a.rect(532, 309, 9, 26, "#B18250", "#3D2C2A", 2, 3)
    a.ellipse(536, 319, 6, 4, PAPER, BRONZE)
    a.line(407, 382, 553, 382, "#E6BD7B", 2)

    # Sconces, their light pools, and a runner laid in perspective.
    for x in (357, 603):
        for r, color in [(31, "#F8BE7420"), (24, "#F8BE7428"), (16, "#F8BE7438")]:
            a.ellipse(x, 240, r * 0.62, r, color)
        a.rect(x - 2, 231, 4, 48, BRONZE, None, 1, 2)
        a.ellipse(x, 271, 7, 5, GILT, INK)
        a.poly([(x - 7, 231), (x + 7, 231), (x + 11, 252), (x - 11, 252)],
               PAPER, "#866140", 1.5)
        a.line(x, 232, x, 250, "#FFE0A4")
    for shift in range(-5, 6):
        a.line(480 + shift * 25, 384, 480 + shift * 92, 540, "#AD845460")
    a.poly([(429, 389), (531, 389), (726, 540), (234, 540)], "#3D2A34", "#95704D", 2)
    a.path("M270 540L441 393H519L690 540M286 540L446 397H514L674 540",
           stroke="#AE7C5270", sw=1.5)
    for y, half, rise in ((406, 15, 4), (428, 24, 6), (460, 37, 9), (510, 61, 16)):
        a.poly([(480 - half, y), (480, y - rise), (480 + half, y), (480, y + rise)],
               "#93534C", "#C68E5D", 1.2)
    for y, half in ((412, 65), (436, 101), (466, 146), (504, 203)):
        a.line(480 - half, y, 480 + half, y, "#BD90632C")
    a.path("M0 0H169L133 122L111 338L0 498Z", "#17192146")
    a.path("M960 0H794L827 122L849 338L960 498Z", "#17192155")


def _office(a):
    wall = a.gradient("office_wall", 180, 0, 840, 420, [
        (0, "#27262C"), (0.6, "#49393A"), (1, "#201D26"),
    ])
    sky = a.gradient("office_sky", 0, 69, 0, 337, [
        (0, "#4A535F"), (0.52, "#776A62"), (1, "#C29264"),
    ])
    wood = a.gradient("office_wood", 273, 375, 690, 474, [
        (0, "#4A3432"), (0.36, "#956247"), (0.56, "#754B3B"), (1, "#382B32"),
    ])
    a.rect(0, 0, 960, 540, wall)
    a.poly([(0, 0), (222, 63), (222, 363), (0, 440)], "#201E27")
    a.poly([(960, 0), (744, 63), (744, 363), (960, 434)], "#201E27")
    a.path("M0 21L222 74H744L960 28M0 407L222 355H744L960 411",
           stroke="#8F6A474F", sw=2)
    a.rect(250, 52, 460, 295, "#242730", "#937152", 2)
    a.rect(263, 65, 434, 267, sky)
    a.ellipse(522, 201, 61, 61, "#D4A87524")
    a.ellipse(522, 201, 41, 41, "#DCB88752")
    a.ellipse(522, 201, 29, 29, "#E4BE8E64")
    for x, y, w, shade in [
        (263, 253, 25, "#4F505B"), (289, 230, 33, "#4D515D"), (324, 253, 29, "#484B58"),
        (356, 214, 39, "#535661"), (396, 241, 36, "#515460"), (437, 206, 44, "#434954"),
        (484, 241, 45, "#4B4B55"), (532, 230, 44, "#454B56"), (579, 191, 43, "#3F4652"),
        (627, 229, 34, "#4A4B55"), (665, 213, 32, "#494B57"),
    ]:
        a.rect(x, y, w, 332 - y, shade)
        a.rect(x + w * 0.28, y - 7, w * 0.46, 7, shade)
        for row, yy in enumerate(range(y + 11, 328, 14)):
            for col, xx in enumerate(range(x + 5, x + w - 3, 9)):
                if (row * 3 + col * 7 + x) % 11 in (1, 2, 4, 8):
                    a.rect(xx, yy, 3, 5, "#E8B777A4")
    a.path("M453 203V183H461V175M603 183V158", stroke="#414653", sw=2)
    a.rect(411, 65, 7, 265, "#252630")
    a.rect(553, 65, 7, 265, "#252630")
    a.line(412, 65, 412, 330, "#B1917265")
    a.line(554, 65, 554, 330, "#B1917265")
    # Venetian slats carry shadow and a narrow reflection across the glass.
    for y in range(78, 331, 18):
        a.rect(264, y, 432, 7, "#282731AA")
        a.line(264, y + 7, 696, y + 7, "#D9B1824B")
    for x in (303, 457, 615, 666):
        a.line(x, 66, x, 330, "#B99A764A")
    a.rect(237, 331, 486, 14, "#25232C", "#7B5E48")
    a.rect(226, 343, 508, 8, "#61483C")

    # Desk and chair silhouettes. Moulding and subtler grain follow the perspective.
    a.path("M476 367V315Q476 289 511 286H589Q622 289 622 315V375Z", "#241F27", "#61483A", 2)
    a.path("M487 361V319Q487 301 514 299H586Q609 301 609 319V371Z", "#382931", "#9B694C", 1.5)
    for x in range(501, 603, 13):
        a.line(x, 312, x, 365, "#AA7A5C2C")
    a.poly([(98, 476), (252, 360), (716, 360), (869, 476)], wood, "#7C5842", 2)
    a.poly([(98, 476), (869, 476), (852, 495), (116, 495)], "#29232C", "#A37753", 1.5)
    a.poly([(121, 496), (851, 496), (818, 540), (150, 540)], "#29232C", "#74523F", 1.5)
    a.path("M135 473L265 371H703L832 473ZM171 517H801M185 525H785",
           stroke="#CC926443", sw=1.5)
    for y, x in ((376, 248), (387, 231), (401, 216), (421, 185), (446, 140), (464, 124)):
        a.line(x, y, 960 - x, y, "#E5A56B22", 1.2)

    # Pool of desk-lamp light, desk blotter and opened file.
    lamp_light = a.gradient("office_lamp_spill", 438, 310, 497, 466, [
        (0, "#EFC07C00"), (0.58, "#EFC07C33"), (1, "#EFC07C00"),
    ])
    a.poly([(407, 308), (493, 308), (703, 463), (273, 463)], lamp_light)
    a.poly([(429, 399), (593, 398), (651, 459), (364, 459)], "#412F33", "#B18257", 2)
    a.poly([(450, 402), (564, 410), (583, 447), (411, 442)], "#C49E65", "#2E282C", 2)
    a.poly([(456, 404), (552, 412), (566, 441), (423, 438)], "#E3C594")
    a.poly([(454, 404), (477, 406), (476, 414), (447, 411)], "#F9DAA5")
    for index in range(5):
        a.line(446 - index * 3, 415 + index * 4, 539 + index * 2, 421 + index * 4, "#725D4966")
    a.line(493, 444, 552, 422, "#2D2930", 3.5)
    a.line(547, 424, 552, 422, "#FFE0A4", 2)
    a.ellipse(389, 408, 39, 10, "#241E25")
    a.ellipse(389, 403, 32, 8, "#9E764A", "#322832", 2)
    a.ellipse(389, 400, 25, 5, "#D2A16A", "#5C4638")
    a.path("M388 401V325Q388 294 413 295H440", stroke="#27242C", sw=13)
    a.path("M388 399V325Q388 294 413 295H440", stroke="#AD8151", sw=6)
    a.path("M387 391V325Q387 294 412 293H439", stroke="#E0B379", sw=1.5)
    a.path("M406 306Q419 274 441 272H466Q485 275 497 306Z", "#42514D", "#23262C", 3)
    a.path("M410 303Q426 271 449 273H466Q479 276 489 288Q455 281 432 300Z", "#647068")
    a.ellipse(452, 307, 46, 6, "#E1B270", "#40302B", 2)
    a.ellipse(452, 309, 36, 4, "#FFE0A6")
    a.line(483, 306, 486, 331, "#AE8359")
    a.ellipse(486, 333, 3, 4, "#CC9A64")

    # A closed spare folder beside the pool of light.
    a.poly([(635, 379), (700, 376), (746, 409), (660, 415)], "#382A31", "#1E1E26", 3)
    a.poly([(634, 376), (699, 373), (740, 403), (656, 409)], "#725245", "#B48259", 1.2)
    a.line(644, 381, 700, 378, "#BF906361")
    a.line(660, 405, 728, 400, "#DBB17A61")
    a.path("M0 0H113L91 333L0 450Z", "#181B2460")
    a.path("M960 0H829L876 333L960 456Z", "#181B247C")


def _museum(a):
    stone = a.gradient("museum_stone", 0, 43, 795, 355, [
        (0, "#282D37"), (0.45, "#6C6460"), (0.68, "#514F53"), (1, "#292D37"),
    ])
    glass = a.gradient("museum_glass", 399, 238, 554, 352, [
        (0, "#F5D49B59"), (0.45, "#BD9F763F"), (1, "#59575C22"),
    ])
    vault = a.gradient("museum_vault", 480, 0, 480, 204, [
        (0, "#383E47"), (0.64, "#766F66"), (1, "#464650"),
    ])
    paving = a.gradient("museum_paving", 480, 338, 480, 540, [
        (0, "#686063"), (1, "#232731"),
    ])
    a.rect(0, 0, 960, 540, INK)
    a.rect(0, 0, 960, 344, stone)
    a.path("M250 346V172C250 65 347 17 480 17C613 17 710 65 710 172V346Z",
           "#A3937B", "#373944", 4)
    a.path("M269 346V177C269 83 353 38 480 38C607 38 691 83 691 177V346Z",
           "#524F55", "#D2AD7A", 2)
    a.path("M281 346V183C281 94 366 57 480 57C594 57 679 94 679 183V346Z", vault)

    # Draw the lines of the barrel roof and its central glazed strip.
    a.path("M308 0L378 151H583L652 0Z", "#9A8E79", "#4F4A4D", 4)
    a.path("M341 0L399 143H559L617 0Z", "#C9AD86", "#5C5759", 4)
    a.path("M409 0L440 144M551 0L519 144M480 0V144",
           stroke="#705F57", sw=4)
    for y, spread in ((28, 125), (56, 112), (84, 100), (108, 89), (129, 81)):
        a.line(480 - spread, y, 480 + spread, y, "#645758", 3)
        a.line(480 - spread, y + 3, 480 + spread, y + 3, "#E8C69572")
    a.path("M0 14Q115 20 265 166M0 45Q99 50 250 180M960 14Q845 20 695 166M960 45Q861 50 710 180",
           stroke="#786B60", sw=3)
    a.path("M0 195L252 223M0 200L252 228M708 223L960 195M708 228L960 200",
           stroke="#C3A484", sw=2)
    a.path("M0 321L252 316M708 316L960 321", stroke="#B49A80", sw=4)
    for x in range(11, 219, 11):
        y = 205 + x * 0.1
        a.line(x, y, x + 24, y + 110, "#B8A58D28")
        a.line(960 - x, y, 936 - x, y + 110, "#B8A58D28")

    # Empty side alcoves and their repeating pilasters.
    for x in (42, 179, 723, 860):
        a.path(f"M{x} 341V219Q{x} 170 {x+38} 161Q{x+76} 169 {x+76} 219V341Z",
               "#30333E", "#AD9172", 2)
        a.path(f"M{x+10} 333V226Q{x+10} 188 {x+38} 182Q{x+66} 188 {x+66} 226V333Z",
               "#232832", "#6D6258", 2)
        a.rect(x + 19, 310, 40, 17, "#534D50", "#A38667")
    for x, w in ((14, 23), (132, 25), (255, 22), (687, 22), (807, 25), (925, 23)):
        a.rect(x, 178, w, 169, "#6A605A", "#A0846C", 1.5)
        a.rect(x + 4, 185, w - 8, 152, "#4A484D")
        for inner in range(x + 7, x + w - 3, 5):
            a.line(inner, 192, inner, 332, "#B29A7937")
        for y in (173, 181, 336, 344):
            a.rect(x - 4, y, w + 8, 5, "#937D65", "#4F484D")

    a.path("M381 344V228Q381 161 480 154Q579 161 579 228V344Z",
           "#454651", "#A0876D", 3)
    a.path("M394 344V228Q394 180 480 167Q566 180 566 228V344Z",
           "#272E3A", "#6B6160", 2)
    a.path("M406 330V234Q406 193 480 179Q554 193 554 234V330Z",
           "#333947", "#9D876E", 1.5)
    for x in (425, 453, 480, 507, 535):
        a.line(x, 194 - (13 - abs(x - 480) * 0.19), x, 329, "#A48E724D", 1.5)
    a.line(408, 232, 552, 232, "#A48E724D")
    a.line(408, 263, 552, 263, "#A48E724D")
    a.rect(0, 345, 960, 195, paving)

    # Joint lines and alternate granite insets grow toward the foreground.
    def joint(column, y):
        return 480 + column * (44 + (y - 345) * 0.63)

    courses = (346, 362, 382, 409, 444, 489, 540)
    for band, (top, bottom) in enumerate(zip(courses, courses[1:])):
        for column in range(-12, 12):
            if (column + band) % 2 == 0:
                a.poly([(joint(column, top), top), (joint(column + 1, top), top),
                        (joint(column + 1, bottom), bottom), (joint(column, bottom), bottom)],
                       "#B19B812F")
        a.line(0, bottom, 960, bottom, "#CFAB7744")
    for column in range(-11, 12):
        a.line(joint(column, 346), 346, joint(column, 540), 540, "#CFAB7755")

    light = a.gradient("museum_light", 460, 160, 490, 425, [
        (0, "#F9D49112"), (0.70, "#FFD89A39"), (1, "#FFD89A07"),
    ])
    a.poly([(421, 146), (538, 146), (635, 435), (327, 435)], light)
    a.ellipse(492, 420, 174, 29, "#14182488")
    a.ellipse(490, 408, 110, 19, "#D9BC8759")
    a.poly([(392, 340), (569, 340), (581, 406), (379, 406)], "#2E3038", "#4C463F", 3)
    a.poly([(388, 336), (557, 336), (571, 352), (375, 352)], "#AE8A5D", "#272A34", 3)
    a.poly([(375, 352), (571, 352), (571, 370), (375, 370)], "#62584D", "#B99362", 2)
    a.poly([(561, 367), (571, 352), (571, 406), (560, 416)], "#272B34", "#6B5C4B", 2)
    a.rect(388, 366, 172, 49, "#60574B", "#A0815A", 2)
    a.rect(402, 375, 144, 27, "#413E43", "#7C6A56", 2)
    a.rect(454, 381, 38, 13, "#BC9765", "#5D4E42")
    a.line(463, 387, 482, 387, "#725F4A")
    a.poly([(390, 348), (398, 246), (544, 246), (558, 348)], glass, "#D5AE77", 2)
    a.poly([(398, 246), (408, 234), (553, 234), (544, 246)], "#FFF0B743", "#DDB884", 1.6)
    a.poly([(544, 246), (553, 234), (568, 333), (558, 348)], "#E4C99D33", "#8B765B", 1.5)
    a.path("M409 258L402 328M418 263L414 310M518 260L525 310M526 258L536 323",
           stroke="#FFE6AE49", sw=2)
    a.poly([(409, 337), (535, 337), (522, 323), (427, 323)], "#E1BD81", "#978266", 1.5)
    a.path("M448 329Q473 310 500 329Q474 339 448 329Z", "#AE8B65")
    a.path("M459 327Q472 322 488 326", stroke="#75624F", sw=1.5)
    a.path("M0 0H128L97 346L0 487Z", "#1519224F")
    a.path("M960 0H830L865 346L960 481Z", "#15192265")


def _theater(a):
    stage = a.gradient("stage", 465, 108, 480, 402, [
        (0, "#262732"), (0.55, "#595044"), (1, "#26252F"),
    ])
    curtain = a.gradient("curtain", 271, 127, 645, 326, [
        (0, "#3D2833"), (0.27, "#945046"), (0.63, "#61383F"), (1, "#3E2834"),
    ])
    floor = a.gradient("stage_floor", 480, 334, 480, 437, [
        (0, "#87694B"), (0.42, "#AE8453"), (1, "#3C3032"),
    ])
    a.rect(0, 0, 960, 540, "#171B26")
    a.path("M0 0H960V291L738 360H224L0 292Z", "#322732")
    a.path("M0 42L259 108H701L960 42M0 61L259 126H701L960 61",
           stroke="#92704E", sw=3)
    a.path("M0 117L225 166M0 201L225 228M735 166L960 117M735 228L960 201",
           stroke="#B08358", sw=2)
    for left, flip in ((0, 1), (960, -1)):
        for offset in (15, 72, 128, 176):
            x = left + flip * offset
            a.poly([(x, 89 + offset * 0.24), (x + flip * 16, 93 + offset * 0.24),
                    (x + flip * 16, 365 - offset * 0.19), (x, 368 - offset * 0.19)],
                   "#4E3A3E", "#8C6B51", 1.3)
        a.poly([(left, 212), (left + flip * 218, 243), (left + flip * 208, 270),
                (left, 261)], "#201E2A", "#82624C", 2.5)
        a.line(left, 227, left + flip * 216, 251, "#AC8056", 1.5)
        for offset in range(16, 202, 13):
            x = left + flip * offset
            a.line(x, 228 + offset * 0.11, x, 259 + offset * 0.045, "#82624C", 1.4)
        a.poly([(left, 293), (left + flip * 218, 293), (left + flip * 213, 315),
                (left, 330)], "#23212B", "#82624C", 2)
        for offset in range(16, 200, 13):
            x = left + flip * offset
            a.line(x, 294, x, 327 - offset * 0.066, "#96704E", 1.2)
        for offset in (46, 149):
            x = left + flip * offset
            y = 177 + offset * 0.24
            a.ellipse(x, y, 22, 30, "#FFD59127")
            a.ellipse(x, y, 16, 20, "#FFD59124")
            a.line(x, y + 22, x, y + 3, GILT, 2)
            a.poly([(x - 6, y - 7), (x + 6, y - 7), (x + 9, y + 5), (x - 9, y + 5)],
                   "#E2BC7D", "#9C7556")

    a.path("M210 389V180Q210 70 480 66Q750 70 750 180V389Z",
           "#6C4C41", "#1C1B25", 7)
    a.path("M226 388V182Q226 89 480 82Q734 89 734 182V388Z",
           "#45323A", "#C89A62", 3)
    a.path("M240 387V184Q240 103 480 98Q720 103 720 184V387Z",
           stage, "#A47853", 3)
    a.path("M255 383V185Q255 118 480 114Q705 118 705 185V383Z",
           "#39343D", "#7B5749", 1.5)
    a.rect(254, 190, 452, 183, stage)
    a.path("M282 340V200L345 150H614L676 200V340", stroke="#69574B", sw=2)
    a.path("M302 343V210L358 175H601L655 212V341", stroke="#AE90664B", sw=1.5)
    a.path("M343 342H617M332 334H627", stroke="#DBC39156", sw=2)
    for x in (360, 385, 408, 552, 575, 600):
        a.line(x, 181, x - (480 - x) * 0.13, 330, "#CAAB7D34")
    light = a.gradient("stage_light", 480, 152, 480, 380, [
        (0, "#EEC78B04"), (0.82, "#EEC78B52"), (1, "#FFD5961C"),
    ])
    a.poly([(465, 148), (493, 148), (585, 365), (369, 365)], light)
    a.poly([(280, 334), (679, 334), (755, 411), (202, 411)], floor, "#976F50", 2.5)
    a.ellipse(480, 376, 134, 19, "#E8B97743")
    a.ellipse(480, 374, 108, 13, "#E4B87831")
    for x in range(283, 686, 25):
        a.line(x, 337, 480 + (x - 480) * 1.39, 410, "#352A2D9C", 1.2)
    for y, half in ((354, 216), (372, 236), (396, 253)):
        a.line(480 - half, y, 480 + half, y, "#C6A07650")

    # Velvet folds and tied panels leave a clear view of the lit stage.
    a.path("M246 171Q282 117 352 123Q329 177 347 219Q360 264 404 337Q363 360 253 347Z",
           curtain, "#422C35", 4)
    a.path("M714 171Q678 117 608 123Q631 177 613 219Q600 264 556 337Q597 360 707 347Z",
           curtain, "#382733", 4)
    for left, flip in ((246, 1), (714, -1)):
        for offset in (12, 27, 46, 61, 78):
            x = left + offset * flip
            a.path(f"M{x} {158 - offset * 0.31}Q{x + flip * 14} 226 {x + flip * 8} 247"
                   f"Q{x + flip * 17} 303 {x + flip * 41} 347",
                   stroke="#3B2936", sw=4)
            a.path(f"M{x + flip * 5} {158 - offset * 0.31}Q{x + flip * 19} 226 {x + flip * 13} 246"
                   f"Q{x + flip * 22} 303 {x + flip * 46} 346",
                   stroke="#E09A677C", sw=1.3)
        a.path(f"M{left + flip * 4} 261Q{left + flip * 39} 275 {left + flip * 105} 257",
               stroke="#D3A469", sw=3.8)
        a.line(left + flip * 84, 263, left + flip * 89, 285, "#CD9D61", 2)
        a.ellipse(left + flip * 90, 288, 4, 7, "#C59361", "#572E32")
    a.path("M248 161Q313 107 378 112Q480 80 582 112Q647 107 712 161Q657 165 613 144"
           "Q539 186 480 132Q421 186 347 144Q303 165 248 161Z", "#7F4542", "#3E2832", 3)
    a.path("M257 159Q324 153 346 132Q417 168 480 119Q543 168 614 132Q636 153 703 159",
           stroke="#AE7553", sw=2.5)
    a.path("M268 149Q320 142 353 125Q418 147 480 112Q542 147 607 125Q640 142 692 149",
           stroke="#C38A5B", sw=1.5)
    a.rect(204, 410, 550, 12, "#231E29", "#A07B50", 2)
    for x in range(225, 735, 29):
        a.ellipse(x, 411, 7, 6, "#F2C17D46")
        a.ellipse(x, 410, 3, 3, "#EDC183")

    # Velvet seats overlap in rows toward the viewer. The central aisle stays open.
    for y, step, width, height, rise, shade in (
        (437, 38, 35, 23, 7, "#3D2B35"),
        (471, 51, 47, 30, 11, "#392732"),
        (515, 70, 65, 39, 15, "#301F2C"),
        (567, 93, 88, 52, 18, "#281E2C"),
    ):
        for index in range(-12, 13):
            if abs(index) < 2:
                continue
            x = 480 + index * step - width / 2
            if x < -width or x > 960:
                continue
            a.path(f"M{x} {y}V{y - height}Q{x + width / 2} {y - height - rise} {x + width} {y - height}"
                   f"V{y}Z", shade, "#181C26", 2)
            a.path(f"M{x + 5} {y - height + 3}Q{x + width / 2} {y - height - rise + 4} "
                   f"{x + width - 5} {y - height + 3}", stroke="#9E63524F", sw=1.5)
    a.path("M0 0H139L103 335L18 540H0Z", "#10172256")
    a.path("M960 0H828L854 334L943 540H960Z", "#10172265")


_SCENES = {
    "HOTEL": _hotel,
    "OFFICE": _office,
    "MUSEUM": _museum,
    "THEATER": _theater,
}


def draw(scene):
    artwork = Art()
    _SCENES[scene](artwork)
    return artwork
