"""Small path-based SVG drawing toolkit and Android VectorDrawable exporter."""

from xml.etree import ElementTree as ET

ANDROID = "http://schemas.android.com/apk/res/android"
AAPT = "http://schemas.android.com/aapt"
ET.register_namespace("android", ANDROID)
ET.register_namespace("aapt", AAPT)


def tag(namespace, name):
    return "{" + namespace + "}" + name


def number(value):
    return format(value, ".3f").rstrip("0").rstrip(".") or "0"


def separate_alpha(color):
    if color.startswith("#") and len(color) == 9:
        return color[:7], number(int(color[7:], 16) / 255)
    return color, None


class Art:
    def __init__(self, width=960, height=540):
        self.width = width
        self.height = height
        self.paths = []
        self.gradients = {}

    def path(self, d, fill=None, stroke=None, sw=1, *, even_odd=False):
        attrs = {"d": d, "fill": fill or "none"}
        if stroke:
            attrs.update({"stroke": stroke, "stroke-width": number(sw),
                          "stroke-linecap": "round", "stroke-linejoin": "round"})
        if even_odd:
            attrs["fill-rule"] = "evenodd"
        if fill or stroke:
            self.paths.append(attrs)

    def rect(self, x, y, w, h, fill, stroke=None, sw=1, rx=0):
        x2, y2 = x + w, y + h
        r = min(rx, w / 2, h / 2)
        n = number
        if r > 0:
            arc = f"A{n(r)} {n(r)} 0 0 1"
            d = (f"M{n(x + r)} {n(y)}H{n(x2 - r)}{arc} {n(x2)} {n(y + r)}"
                 f"V{n(y2 - r)}{arc} {n(x2 - r)} {n(y2)}H{n(x + r)}"
                 f"{arc} {n(x)} {n(y2 - r)}V{n(y + r)}{arc} {n(x + r)} {n(y)}Z")
        else:
            d = f"M{n(x)} {n(y)}H{n(x2)}V{n(y2)}H{n(x)}Z"
        self.path(d, fill, stroke, sw)

    def line(self, x1, y1, x2, y2, stroke, sw=1):
        self.path(f"M{number(x1)} {number(y1)}L{number(x2)} {number(y2)}", stroke=stroke, sw=sw)

    def ellipse(self, cx, cy, rx, ry, fill, stroke=None, sw=1):
        if rx <= 0 or ry <= 0:
            return
        arc = f"A{number(rx)} {number(ry)} 0 1 1"
        left, right, y = number(cx - rx), number(cx + rx), number(cy)
        self.path(f"M{left} {y}{arc} {right} {y}{arc} {left} {y}Z", fill, stroke, sw)

    def poly(self, points, fill=None, stroke=None, sw=1):
        d = "M" + "L".join(f"{number(x)} {number(y)}" for x, y in points) + "Z"
        self.path(d, fill, stroke, sw)

    def gradient(self, id, x1, y1, x2, y2, stops):
        self.gradients[id] = (x1, y1, x2, y2, tuple(stops))
        return f"url(#{id})"

    def svg(self):
        root = ET.Element("svg", {
            "xmlns": "http://www.w3.org/2000/svg",
            "viewBox": f"0 0 {self.width} {self.height}",
            "width": str(self.width),
            "height": str(self.height),
        })
        defs = ET.SubElement(root, "defs") if self.gradients else None
        for id, (x1, y1, x2, y2, stops) in self.gradients.items():
            gradient = ET.SubElement(defs, "linearGradient", {
                "id": id, "gradientUnits": "userSpaceOnUse",
                "x1": number(x1), "y1": number(y1), "x2": number(x2), "y2": number(y2),
            })
            for offset, color in stops:
                rgb, alpha = separate_alpha(color)
                attrs = {"offset": number(offset), "stop-color": rgb}
                if alpha is not None:
                    attrs["stop-opacity"] = alpha
                ET.SubElement(gradient, "stop", attrs)
        for attrs in self.paths:
            attrs = attrs.copy()
            for prop in ("fill", "stroke"):
                if prop in attrs:
                    attrs[prop], alpha = separate_alpha(attrs[prop])
                    if alpha is not None:
                        attrs[prop + "-opacity"] = alpha
            ET.SubElement(root, "path", attrs)
        return root

    def android(self):
        # Export the SVG's same paths and paint values using Android resource syntax.
        svg = self.svg()
        root = ET.Element("vector", android_attrs(
            width=f"{self.width}dp", height=f"{self.height}dp",
            viewportWidth=str(self.width), viewportHeight=str(self.height)))
        gradients = {element.get("id"): element for element in svg.findall("./defs/linearGradient")}
        for element in svg.findall("path"):
            attrs = {"pathData": element.get("d")}
            for svg_name, android_name in (("stroke-width", "strokeWidth"),
                                           ("stroke-linecap", "strokeLineCap"),
                                           ("stroke-linejoin", "strokeLineJoin"),
                                           ("stroke-opacity", "strokeAlpha"),
                                           ("fill-opacity", "fillAlpha")):
                if element.get(svg_name) is not None:
                    attrs[android_name] = element.get(svg_name)
            if element.get("fill-rule") == "evenodd":
                attrs["fillType"] = "evenOdd"
            path = ET.SubElement(root, "path", android_attrs(**attrs))
            for prop in ("fill", "stroke"):
                value = element.get(prop, "none")
                if value == "none":
                    continue
                attr_name = prop + "Color"
                if value.startswith("url(#"):
                    attr = ET.SubElement(path, tag(AAPT, "attr"), {"name": "android:" + attr_name})
                    add_android_gradient(attr, gradients[value[5:-1]])
                else:
                    path.set(tag(ANDROID, attr_name), value)
        return root


def android_attrs(**attrs):
    return {tag(ANDROID, name): value for name, value in attrs.items()}


def add_android_gradient(parent, gradient):
    grad = ET.SubElement(parent, "gradient", android_attrs(
        type="linear", startX=gradient.get("x1"), startY=gradient.get("y1"),
        endX=gradient.get("x2"), endY=gradient.get("y2")))
    for stop in gradient:
        rgb = stop.get("stop-color")
        alpha = round(float(stop.get("stop-opacity", "1")) * 255)
        color = f"#{alpha:02x}{rgb[1:]}"
        ET.SubElement(grad, "item", android_attrs(offset=stop.get("offset"), color=color))
