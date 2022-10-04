import os
import ruamel.yaml


yaml = ruamel.yaml.YAML()


with open("config.yml", encoding="utf8") as f:
    data = yaml.load(f)


for p_id, panel in data["panels"].items():
    for idx, slot in enumerate(panel["slots"]):
        icon = slot["icon"]
        if "displayname" in icon and "lorelines" in icon:
            continue
        item = icon["itemstack"]
        meta = item.get("meta", {})
        changed = False

        display_name = meta.get("display-name")
        if display_name:
            icon["displayname"] = display_name
            meta.pop("display-name")
            changed = True

        lore_lines = meta.get("lore")
        if lore_lines:
            icon["lorelines"] = lore_lines
            meta.pop("lore")
            changed = True

        if changed:
            print(f"Updated {p_id} panel[{idx}]")


with open("config.converted.yml", "w", encoding="utf8") as f:
    yaml.dump(data, f)


os.rename("config.yml", "config.org.yml")
print("Saved config.converted.yml")
