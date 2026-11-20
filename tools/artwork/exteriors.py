"""Engraved environments for the garden, harbor, laboratory, and observatory."""

from random import Random

from vector import Art


def garden(a):
    horizon = a.gradient(
        "garden_dusk", 0, 0, 0, 370,
        [(0, "#252C32"), (0.48, "#5E5B4C"), (1, "#B89365")],
    )
    a.rect(0, 0, 960, 540, horizon)
    a.ellipse(580, 146, 84, 83, "#BB95654D")
    a.ellipse(580, 146, 63, 63, "#CCAE7980")
    a.ellipse(580, 146, 44, 44, "#D6BD8F")
    a.path("M0 268 Q155 214 285 255 T578 247 T960 225 V400 H0 Z", "#49493E")
    a.path("M0 313 Q177 253 394 297 T960 273 V417 H0 Z", "#353F39")

    random = Random(54)
    for _ in range(87):
        x = random.randrange(0, 960)
        y = random.randrange(235, 310)
        length = random.randrange(2, 12)
        a.line(x, y, x + length / 3, y - length, "#A88A5A50", 1.2)

    # The conservatory and doorway sit on the axis of the garden's stone walk.
    glass = a.gradient(
        "garden_glass", 400, 200, 560, 325,
        [(0, "#9A997B"), (0.54, "#7F816C"), (1, "#4B5046")],
    )
    a.path("M296 331 V253 L365 218 L414 163 L508 126 L598 165 L627 216 L668 260 V331 Z",
           "#2D3634", "#C1A173", 3)
    a.path("M314 324 V266 L379 231 L424 175 L507 145 L586 177 L614 227 L650 266 V324 Z",
           glass, "#20282A", 2)
    a.path("M296 254 L365 218 L627 217 L668 258 Z",
           "#514B3E", "#BB9C70", 2)
    a.path("M415 219 L439 176 L507 147 L576 178 L599 218 M379 219 H620",
           None, "#D2B083", 2.4)
    for x, high in [(332, 257), (361, 243), (390, 227), (426, 218), (459, 168),
                    (487, 153), (529, 155), (554, 163), (583, 178), (611, 221), (636, 250)]:
        a.line(x, high, x, 326, "#303632", 3)
    for y in (263, 285, 308):
        a.line(314, y, 649, y, "#CCAD784D", 1.4)
    a.path("M450 335 V267 Q450 237 506 225 Q563 237 563 267 V335 Z",
           "#293330", "#C5A376", 2)
    a.path("M464 326 V268 Q466 251 506 240 Q545 250 549 268 V326 Z",
           "#9A8053", "#C4AB79", 1.5)
    a.line(506, 243, 506, 325, "#4A4939", 2.5)
    for x in (473, 485, 497, 516, 529, 541):
        a.line(x, 256, x, 322, "#4A4939", 1.5)

    a.path("M0 342 Q217 311 418 335 L607 334 Q799 314 960 345 V540 H0 Z", "#293431")
    a.path("M463 326 H543 Q544 376 505 406 Q468 440 576 468 Q639 487 716 540 H218 "
           "Q257 489 338 456 Q414 422 425 393 Q439 362 463 326 Z",
           "#A08B67", "#CAA574", 1.3)
    a.path("M487 328 H539 L519 378 Q511 398 493 415 Q481 444 563 462 "
           "Q653 489 691 540 H338 Q401 481 437 455 Q471 427 455 406 "
           "Q445 386 487 328 Z", "#B39A74")
    for y, left, right in [(339, 458, 543), (353, 449, 539), (373, 435, 527), (396, 424, 515),
                           (421, 406, 494), (448, 354, 524), (476, 298, 589), (511, 247, 668)]:
        a.path(f"M{left} {y} Q{(left + right) / 2} {y - 10} {right} {y + 2}", None, "#625B46", 1.4)

    # Close vegetation and iron railing give the scene a shadowed foreground.
    a.path("M0 312 Q56 278 151 318 Q228 314 284 370 Q344 365 365 414 "
           "Q272 456 226 540 H0 Z", "#1E2A28")
    a.path("M960 288 Q903 300 869 334 Q800 333 772 370 Q721 390 739 441 "
           "Q855 478 868 540 H960 Z", "#202B27")
    for left, right, baseline in [(0, 298, 417), (755, 960, 408)]:
        a.path(f"M{left} {baseline + 9} Q{(left + right) / 2} {baseline - 24} {right} {baseline + 2}",
               None, "#897A56", 3)
        for x in range(left + 8, right, 22):
            dy = abs((x - (left + right) / 2) / (right - left)) * 27
            y = baseline - 9 + dy
            a.line(x, y, x, y + 39, "#0E181B", 4.5)
            a.line(x + 1.6, y + 3, x + 1.6, y + 28, "#A88B5B6B", 1)
    for _ in range(220):
        x = random.randrange(0, 960)
        y = random.randrange(359, 550)
        if x > 742 or x < 223 or (x < 347 and y < 435):
            length = 5 + (y - 359) / 24 + random.random() * 9
            end = x + random.uniform(-10, 11)
            a.line(x, y, end, y - length, random.choice(("#B1905548", "#80765159", "#72785C68")), 1)
    for x, lean in ((42, 66), (121, -32), (915, -64)):
        a.path(f"M{x - 16} 540 Q{x + 16} 386 {x - lean} 205 Q{x - lean * 0.8} 146 {x - lean} 0 "
               f"H{x - lean + 36} Q{x + 8} 186 {x + 44} 354 L{x + 32} 540 Z",
               "#141D20", "#5B574050", 2)
        a.path(f"M{x + 11} 496 Q{x + 27} 369 {x - lean + 5} 159 T{x - lean + 20} 9",
               None, "#8C745342", 2)
    a.path("M0 37 Q121 -20 207 54 Q241 32 315 84 Q210 65 180 88 "
           "Q156 114 93 103 Q0 148 0 124 Z", "#172124")
    a.path("M960 31 Q864 2 793 44 Q728 33 675 73 Q811 56 826 105 "
           "Q906 88 960 155 Z", "#172124")


def harbor(a):
    fog = a.gradient(
        "harbor_fog", 110, 0, 50, 415,
        [(0, "#283239"), (0.45, "#5C655D"), (0.72, "#ACA082"), (1, "#534F43")],
    )
    water = a.gradient(
        "harbor_water", 0, 246, 0, 540,
        [(0, "#7F7764"), (0.48, "#4B5753"), (1, "#17292D")],
    )
    a.rect(0, 0, 960, 540, fog)
    a.path("M0 181 L82 169 L82 157 L105 157 L122 166 L174 166 L190 175 L245 177 "
           "L291 164 L351 163 L388 186 L535 186 L545 174 L576 163 L636 156 L666 176 "
           "L725 173 L751 155 L782 176 L834 177 L853 161 L897 150 L911 167 L960 164 V262 H0 Z",
           "#414947")
    a.path("M0 206 L190 200 L312 220 L451 201 L671 214 L814 191 L960 205 V274 H0 Z", "#2F3C3E")
    a.rect(0, 257, 960, 283, water)
    a.path("M0 258 Q163 257 372 273 T960 253", None, "#D3B48275", 1.4)

    for x, y, beam in [(91, 183, 88), (292, 213, 68), (733, 189, -86), (816, 184, 91)]:
        top = y - 80
        a.line(x, y + 9, x, top, "#3A4442", 4)
        a.path(f"M{x - 12} {y} L{x} {top} L{x + beam} {top + 24} L{x - 8} {top + 10} Z",
               None, "#3A4442", 2)
        a.line(x + beam, top + 24, x + beam, top + 54, "#3A4442", 1.3)
        a.line(x - 16, y + 2, x + 17, y + 2, "#3A4442", 4)

    random = Random(81)
    for _ in range(155):
        x = random.randrange(0, 960)
        y = random.randrange(267, 544)
        length = random.uniform(2, 28) * (0.6 + (y - 257) / 250)
        a.line(x, y, x + length, y - random.uniform(0, 1.7),
               random.choice(("#C1AF8052", "#9BA38B57", "#141F2885")), random.choice((0.8, 1.2, 1.6)))
    for x in (46, 102, 167, 237, 296, 337, 661, 747, 798, 879, 914):
        a.rect(x - 2, 220 + x % 9, 4, 2, "#BFA76E")
        for line in range(3):
            y = 265 + line * (6 + x % 4)
            a.line(x - 3 - line * 2, y, x + 5 + line * 2, y, "#D7AD6261", 1.2)

    # An old working vessel, low in the water and sharply outlined against the fog.
    hull = a.gradient(
        "harbor_hull", 0, 275, 0, 382,
        [(0, "#3D4441"), (0.64, "#222C31"), (1, "#121F27")],
    )
    a.path("M169 305 Q292 312 445 290 L692 260 Q732 255 773 234 "
           "Q736 303 698 331 Q613 357 290 390 L209 370 Z",
           hull, "#141E23", 3)
    a.path("M187 326 Q438 322 717 288 L739 279 Q720 310 696 329 "
           "Q595 360 290 386 L224 368 Z", "#704B40")
    a.path("M187 325 Q431 319 739 278 M222 354 Q451 359 720 306",
           None, "#C7A67A", 2)
    a.path("M268 305 L276 277 L538 240 L640 238 L684 259 Z",
           "#445047", "#C3AE81", 1.5)
    a.path("M338 268 V230 L359 213 L471 205 L508 227 L512 248 Z",
           "#6F7058", "#1B2A30", 3)
    a.path("M350 234 L495 220 L500 238 L350 253 Z", "#353D39", "#A79062", 2)
    for x in range(356, 490, 16):
        y = 237 - (x - 356) * 0.092
        a.poly([(x, y), (x + 10, y - 1), (x + 10, y + 6), (x, y + 7)], "#BF9C61")
    a.path("M412 211 L409 177 L439 173 L447 208 Z", "#353D3C", "#A8976F", 2)
    a.path("M408 176 L439 170 L445 180 L407 186 Z", "#22292B")
    a.path("M541 249 L561 115 L567 116 L556 246 Z", "#2A3436", "#C5AA7E", 1.3)
    a.line(561, 124, 632, 237, "#293C3F", 1.6)
    a.line(561, 124, 488, 239, "#293C3F", 1.35)
    a.line(541, 160, 583, 158, "#293C3F", 2.5)
    a.ellipse(566, 113, 3, 3, "#E2B875")
    for i in range(11):
        x = 273 + i * 39
        y = 326 - i * 4.55
        a.ellipse(x, y, 3.7, 3.2, "#111D22", "#A48A60", 1.2)
        a.line(x, y + 17, x + 11, y + 26, "#D1AA7053", 1)

    a.path("M0 376 L89 391 L400 540 H0 Z", "#18252B", "#9C8967", 2)
    a.path("M0 386 L75 397 L359 540 H0 Z", "#433D35")
    for x in range(-13, 90, 13):
        a.line(x, 392, x * 4 + 29, 540, "#A8926959", 1.5)
    for x, y, size in [(39, 425, 35), (121, 453, 20), (280, 525, 32), (842, 470, 27), (922, 415, 29)]:
        a.path(f"M{x} {y} L{x + size * 0.67} {y - 7} L{x + size} {y} "
               f"L{x + size * 0.8} {y + 110} H{x + 4} Z", "#161E23", "#6F6753", 1.6)
        a.ellipse(x + size / 2, y, size / 2, size / 5, "#62584A", "#7F7255", 1.5)
    a.path("M60 438 Q168 507 208 409 Q230 369 281 357 M843 477 Q779 465 709 314",
           None, "#161E23", 5)
    a.path("M60 437 Q168 505 207 408 Q230 369 281 357 M843 476 Q779 463 709 314",
           None, "#AD8C5D", 1.7)


def bottle(a, x, y, w, h, tone):
    a.path(f"M{x + w * 0.3} {y + 11} V{y + h * 0.29} "
           f"Q{x + 1} {y + h * 0.39} {x + 1} {y + h * 0.53} V{y + h - 7} "
           f"Q{x + w / 2} {y + h + 4} {x + w - 1} {y + h - 7} V{y + h * 0.53} "
           f"Q{x + w - 1} {y + h * 0.39} {x + w * 0.7} {y + h * 0.29} V{y + 11} Z",
           tone, "#E4C590", 1.2)
    a.ellipse(x + w * 0.5, y + h - 8, w * 0.43, 4, "#171E24", "#AF9263", 0.9)
    a.line(x + w * 0.18, y + h * 0.48, x + w * 0.16, y + h * 0.86, "#F5DFAD7A", 1.4)
    a.rect(x + w * 0.24, y + 4, w * 0.52, 11, "#856D4E", "#CCAA76", 1)
    a.ellipse(x + w / 2, y + 4, w * 0.26, 2.4, "#BE9E69", "#E7C795", 0.7)


def lab(a):
    wall = a.gradient(
        "lab_wall", 240, 0, 624, 400,
        [(0, "#222A2D"), (0.52, "#655C4B"), (1, "#333E3D")],
    )
    a.rect(0, 0, 960, 540, wall)
    light = a.gradient(
        "lab_window", 410, 11, 382, 334,
        [(0, "#7F806B"), (0.6, "#B09D75"), (1, "#DCC396")],
    )
    a.path("M281 323 V46 Q281 18 309 18 H657 Q681 18 681 48 V323 Z",
           "#20292B", "#9E8764", 3.5)
    a.rect(305, 41, 350, 271, light, "#C4A779", 2)
    for x in (367, 480, 593):
        a.line(x, 42, x, 311, "#5D604D", 6)
        a.line(x + 2.5, 42, x + 2.5, 311, "#D4B889", 1.5)
    for y in (127, 221):
        a.line(305, y, 655, y, "#5D604D", 7.5)
        a.line(305, y + 3, 655, y + 3, "#CFAC78", 1.5)
    for x0, x1 in ((316, 358), (380, 470), (492, 583), (605, 644)):
        for y in range(49, 309, 15):
            a.line(x0, y + 9, x1, y - 7, "#F1D5A561", 1)
    a.poly([(308, 319), (652, 317), (789, 472), (236, 492)], "#C2A47148")

    for left, right in ((0, 267), (692, 960)):
        for y in (150, 263):
            a.rect(left, y, right - left, 9, "#252B2A", "#BE9F6E", 1.4)
            a.rect(left, y + 9, right - left, 7, "#151F25")
    for x, y, w, h, tint in [
        (34, 89, 38, 59, "#41413A"), (101, 66, 54, 84, "#585641"), (177, 81, 40, 69, "#343C39"),
        (723, 94, 36, 58, "#393B34"), (766, 73, 46, 79, "#564736"), (861, 61, 62, 92, "#31393B"),
        (35, 181, 58, 84, "#3B4942"), (166, 192, 37, 72, "#69473B"), (216, 210, 28, 54, "#343A39"),
        (739, 201, 36, 63, "#6B573E"), (812, 175, 52, 90, "#42443C"), (901, 196, 40, 68, "#71644A"),
    ]:
        bottle(a, x, y, w, h, tint)

    bench = a.gradient(
        "lab_bench", 0, 338, 0, 540,
        [(0, "#635743"), (0.42, "#5A4A37"), (1, "#282D29")],
    )
    a.path("M113 321 H842 L960 455 V540 H0 V453 Z", bench, "#181D21", 2.5)
    a.poly([(349, 322), (605, 322), (732, 478), (219, 478)], "#BF9B674C")
    for y, width in [(365, 786), (389, 835), (418, 882), (456, 939), (522, 960)]:
        a.line((960 - width) / 2, y, (960 + width) / 2, y - 5, "#2D2E29", 1.5)

    # Stand with glassware and a burner, backlit enough to show etched glass edges.
    a.ellipse(489, 441, 118, 16, "#201F1EAB")
    a.path("M344 415 L555 414 L579 437 L331 443 Z", "#1C262A", "#9B805A", 1.8)
    a.path("M371 419 L371 174 L378 169 L382 418 Z", "#2C3434", "#D0AD77", 1.4)
    a.ellipse(376, 173, 5, 5, "#BBA26C", "#393C35", 1)
    a.line(378, 269, 489, 249, "#292F31", 7.7)
    a.line(378, 266, 489, 246, "#BC9E6B", 1.4)

    glass = a.gradient(
        "lab_liquid", 449, 269, 562, 383,
        [(0, "#9E7951"), (0.45, "#88623E"), (1, "#4C392C")],
    )
    a.path("M469 206 H511 L512 260 Q523 282 553 328 Q571 359 558 380 "
           "Q545 393 490 395 Q438 393 423 378 Q410 363 429 329 "
           "Q455 283 468 259 Z",
           "#FBE0A645", "#F1D7A6", 2.5)
    a.path("M437 314 Q491 329 545 314 Q583 370 557 379 "
           "Q541 391 490 391 Q435 389 424 375 Q411 355 437 314 Z",
           glass, "#DED0A0", 1.1)
    a.ellipse(491, 316, 53, 8, "#C6A77590", "#F0D3A0A1", 1.3)
    a.ellipse(490, 207, 21, 4.3, "#242C2F", "#F5DBAD", 1.5)
    a.path("M475 214 V260 Q469 284 447 311 M424 362 Q428 376 449 380",
           None, "#F4E3BD", 2.2)
    a.path("M423 189 Q433 177 458 183 L519 197 Q537 202 526 212 "
           "L470 247 Q464 250 456 246 Q451 242 457 237 L493 212 L425 199 Z",
           "#E5C89543", "#EBD4A9", 1.7)
    a.path("M431 187 L494 202 Q504 206 494 213", None, "#F6E1BA", 1.2)
    for x, y in ((471, 351), (475, 337), (518, 368), (492, 362), (503, 344)):
        a.ellipse(x, y, 2.3, 2.8, "#E1C18175", "#F6DCAB9E", 1)

    bottle(a, 596, 289, 77, 137, "#3E3D2F")
    bottle(a, 671, 324, 53, 98, "#613F32")
    a.path("M130 418 L255 392 L355 428 L229 470 Z", "#161E24", "#B1986F", 2)
    a.path("M133 415 L244 386 L353 425 L230 463 Z", "#BAA476", "#EEE0B5", 1.3)
    a.path("M244 386 Q248 406 230 463 M134 419 L230 461 L350 427", None, "#694F38", 1.3)
    for i in range(1, 8):
        fraction = i / 9
        spine_x = 244 - 14 * fraction
        spine_y = 387 + 74 * fraction
        a.line(151 + 78 * fraction, 414 + 41 * fraction,
               spine_x - 5, spine_y + 1.2, "#6D6455", 0.8)
        a.line(spine_x + 5, spine_y + 1.5,
               336 - 101 * fraction, 424 + 33 * fraction, "#6D6455", 0.8)
    random = Random(63)
    for _ in range(35):
        x = random.randint(10, 937)
        y = random.randint(480, 550)
        a.line(x, y, x + random.randrange(5, 29), y - 1, "#BA97654D", 1)


def observatory(a):
    sky = a.gradient(
        "observatory_sky", 0, 5, 0, 420,
        [(0, "#141F2B"), (0.53, "#35434A"), (1, "#9A8263")],
    )
    a.rect(0, 0, 960, 540, sky)
    random = Random(103)
    for _ in range(87):
        x, y = random.uniform(12, 948), random.uniform(9, 270)
        size = random.uniform(0.6, 1.9)
        a.ellipse(x, y, size, size, random.choice(("#EDCF95", "#9CA8A1", "#C3A986")))
    for x, y in [(129, 111), (247, 73), (431, 125), (673, 47), (810, 227)]:
        a.line(x - 5, y, x + 5, y, "#EAD4A48F", 0.8)
        a.line(x, y - 5, x, y + 5, "#EAD4A48F", 0.85)
    a.ellipse(714, 99, 42, 42, "#CAB48533")
    a.ellipse(714, 99, 33, 33, "#E1CDA1")
    a.path("M703 68 Q709 90 703 108 Q700 119 684 116 Q672 88 703 68 Z", "#C9B386")
    for cx, cy, rx, ry in [(723, 106, 7, 5), (736, 90, 3, 4), (718, 80, 5, 3), (721, 117, 3, 2)]:
        a.ellipse(cx, cy, rx, ry, "#B9A38073")

    a.path("M0 375 L118 274 L164 303 L216 267 L293 331 L375 289 L491 335 L571 299 "
           "L620 330 L755 232 L819 288 L867 266 L960 353 V466 H0 Z", "#44504B")
    a.path("M0 407 L168 343 L207 364 L264 319 L366 397 L448 351 L529 391 L642 348 "
           "L731 386 L842 312 L960 354 V540 H0 Z", "#303C3C")
    for _ in range(76):
        x = random.uniform(0, 960)
        y = random.uniform(363, 409)
        a.line(x, y, x + random.randrange(6, 21), y - 2, "#B49D6B48", 1)
    a.path("M0 456 Q199 403 365 430 Q515 405 645 404 Q813 412 960 457 V540 H0 Z", "#252E30")

    dome = a.gradient(
        "observatory_dome", 352, 192, 642, 397,
        [(0, "#8E8068"), (0.52, "#595B51"), (1, "#313D40")],
    )
    a.path("M336 332 Q341 241 410 200 Q448 178 493 178 Q569 181 617 239 "
           "Q647 278 650 331 Z", dome, "#C6AD7E", 2.3)
    for offset in (8, 23, 39, 56, 72, 89):
        a.path(f"M{343 + offset} 329 Q{357 + offset} {215 - offset * 0.35} {493 + offset * 0.23} {177 + offset * 0.52} "
               f"Q{577 + offset * 0.2} {207 + offset * 0.29} {640 - offset * 0.51} 329",
               None, "#DDD0A37A", 1.16)
    a.path("M475 180 L519 180 Q568 214 587 324 H542 Q521 228 475 180 Z",
           "#202A2E", "#C4AC7E", 2)
    a.path("M492 180 Q542 239 558 320", None, "#9F9372", 1.7)
    a.poly([(529, 261), (635, 174), (647, 187), (542, 273)], "#222D33", "#D8C192", 2)
    a.poly([(624, 182), (639, 167), (653, 184), (638, 199)], "#4E5652", "#E6C190", 1.4)
    a.poly([(634, 172), (644, 162), (658, 179), (650, 188)], "#1D282E", "#E6C190", 1.7)
    a.ellipse(541, 260, 9, 11, "#8E754F", "#DBC494", 1.4)

    a.poly([(332, 331), (650, 328), (648, 435), (338, 442)], "#505349", "#AE9369", 1.8)
    a.path("M332 334 L651 331 L660 346 L324 351 Z", "#27343A", "#BAA478", 2)
    for x in range(342, 644, 16):
        a.line(x, 354, x + 1, 429, "#D0B48749", 0.9)
    a.path("M450 440 V374 Q452 356 472 355 Q493 356 496 374 V442 Z",
           "#252C2D", "#C4A778", 1.7)
    a.path("M456 440 V375 Q459 364 472 362 Q486 364 489 375 V440 Z",
           "#957648", "#D3AA73", 1.3)
    a.line(472, 365, 472, 441, "#303835", 2.4)
    for x in (360, 408, 526, 577, 619):
        a.rect(x, 367, 13, 19, "#2B3032", "#C9AF7E", 1.3)
        a.rect(x + 3, 370, 7, 12, "#B3965C")
    a.path("M445 437 L504 438 L537 455 L536 466 L399 469 L403 459 Z", "#8B785A", "#594F3B", 1.1)

    a.path("M0 455 Q164 442 318 454 L400 450 L425 454 Q329 476 298 540 H0 Z",
           "#1C282D", "#676B54", 1.3)
    a.path("M960 441 Q786 433 671 462 Q589 455 528 480 Q588 504 605 540 H960 Z",
           "#1C272D", "#6A6A53", 1.3)
    a.path("M432 452 Q384 472 378 490 Q369 517 342 540 H573 "
           "Q521 516 473 500 Q430 483 474 465 L504 455 Z",
           "#8B785A", "#B29B70", 1.25)
    for _ in range(96):
        x, y = random.randint(0, 960), random.randint(474, 545)
        if x < 353 or x > 580:
            a.line(x, y, x + random.randint(1, 12), y - random.randint(3, 11), "#A08C666C", 1.1)


def draw(scene):
    art = Art()
    {"GARDEN": garden, "HARBOR": harbor, "LAB": lab, "OBSERVATORY": observatory}[scene](art)
    return art
