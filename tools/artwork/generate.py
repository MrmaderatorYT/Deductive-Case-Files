#!/usr/bin/env python3
"""Generate the game's SVG artwork and matching Android resources."""

from pathlib import Path
from xml.etree import ElementTree as ET
import re
import sys

sys.dont_write_bytecode = True

import cover
import exteriors
import interiors
import streets
from vector import Art


ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main"
RES = APP / "res"
SVG = ROOT / "artwork"


def write_xml(path, element):
    ET.indent(element, space="    ")
    content = ET.tostring(element, encoding="unicode", xml_declaration=True) + "\n"
    path.parent.mkdir(parents=True, exist_ok=True)
    if not path.exists() or path.read_text(encoding="utf-8") != content:
        path.write_text(content, encoding="utf-8")


def export(folder, name, art):
    write_xml(SVG / folder / f"{name}.svg", art.svg())
    write_xml(RES / "drawable" / f"{name}.xml", art.android())


def generate_scenes():
    export("scenes", "scene_hero", cover.draw())
    for module, names in (
        (streets, ("MANOR", "CAFE", "TRAIN", "LIBRARY")),
        (interiors, ("MUSEUM", "THEATER", "HOTEL", "OFFICE")),
        (exteriors, ("GARDEN", "HARBOR", "LAB", "OBSERVATORY")),
    ):
        for scene in names:
            export("scenes", f"scene_{scene.lower()}", module.draw(scene))


def generate_mark():
    mark = Art(108, 108)
    mark.path("M33 29H59V45H75V73L68 80H33Z M63 29L75 41H63Z"
              " M41 44H50C59.5 44 66 50 66 58C66 66.3 59.5 72 50 72H41Z"
              " M47 50V66H50C55.8 66 60 63 60 58C60 53.2 55.8 50 50 50Z",
              "#F2EADD", even_odd=True)
    export("brand", "ic_case_mark", mark)


def generate_evidence():
    import evidence

    case_data = (APP / "assets/cases.dat").read_text(encoding="utf-8")
    specs = set()
    for body in re.split(r"^\[\d+\]$", case_data, flags=re.MULTILINE)[1:]:
        if "k=VISUAL" in body.splitlines():
            match = re.search(r"^v=(.+)$", body, flags=re.MULTILINE)
            if match is None:
                raise ValueError("Visual case is missing its evidence specification")
            specs.add(match[1])
    resources = ET.Element("resources")
    keys = ET.SubElement(resources, "string-array", {"name": "evidence_specs", "translatable": "false"})
    images = ET.SubElement(resources, "array", {"name": "evidence_images"})
    for spec in sorted(specs):
        name = "evidence_" + re.sub(r"[^a-z0-9]+", "_", spec.lower())
        export("evidence", name, evidence.draw(spec))
        ET.SubElement(keys, "item").text = spec
        ET.SubElement(images, "item").text = "@drawable/" + name
    write_xml(RES / "values/evidence_art.xml", resources)


if __name__ == "__main__":
    generate_scenes()
    generate_mark()
    generate_evidence()
