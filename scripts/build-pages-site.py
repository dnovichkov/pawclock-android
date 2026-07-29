#!/usr/bin/env python3
"""Сборка GitHub Pages-сайта из docs/PRIVACY.md (единый источник).

Выход: _site/privacy/index.html (двуязычная политика, EN + RU) и
_site/index.html (redirect на privacy/). Запускается workflow'ом
.github/workflows/pages.yml; локально: `pip install markdown && python
scripts/build-pages-site.py`.

Канонический URL: https://dnovichkov.github.io/pawclock-android/privacy/
"""
import pathlib

import markdown

ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / "docs" / "PRIVACY.md"
OUT = ROOT / "_site"

PAGE_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>PawClock — Privacy Policy · Политика конфиденциальности</title>
<link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🐾</text></svg>">
<style>
:root {{
  --bg: #fafdfb; --fg: #191c1b; --accent: #006a60; --muted: #6f7976;
  --rule: #dae5e1;
}}
@media (prefers-color-scheme: dark) {{
  :root {{ --bg: #101413; --fg: #e0e3e1; --accent: #53dbc9; --muted: #899390;
           --rule: #3f4946; }}
}}
* {{ box-sizing: border-box; }}
body {{
  margin: 0 auto; padding: 2rem 1.25rem 4rem; max-width: 46rem;
  background: var(--bg); color: var(--fg);
  font: 1rem/1.65 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
}}
nav {{ margin-bottom: 2rem; font-size: .95rem; }}
nav a {{ margin-right: 1rem; }}
a {{ color: var(--accent); }}
h1 {{ font-size: 1.7rem; line-height: 1.25; margin-top: 0; }}
h2 {{ font-size: 1.15rem; margin-top: 2rem; }}
em {{ color: var(--muted); font-style: normal; font-size: .9rem; }}
code {{ background: var(--rule); padding: .1em .35em; border-radius: 4px;
        font-size: .9em; }}
hr {{ border: 0; border-top: 1px solid var(--rule); margin: 3rem 0; }}
section {{ scroll-margin-top: 1rem; }}
footer {{ margin-top: 3rem; color: var(--muted); font-size: .85rem; }}
</style>
</head>
<body>
<nav>🐾 <strong>PawClock</strong> &nbsp;·&nbsp;
<a href="#en">English</a> <a href="#ru">Русский</a>
<a href="https://github.com/dnovichkov/pawclock-android">GitHub</a></nav>
<section id="en">
{en}
</section>
<hr>
<section id="ru" lang="ru">
{ru}
</section>
<footer>Generated from
<a href="https://github.com/dnovichkov/pawclock-android/blob/main/docs/PRIVACY.md">docs/PRIVACY.md</a>.</footer>
</body>
</html>
"""

REDIRECT_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="refresh" content="0; url=./privacy/">
<title>PawClock</title>
<link rel="canonical" href="./privacy/">
</head>
<body><p><a href="./privacy/">PawClock — Privacy Policy</a></p></body>
</html>
"""


def render(text: str) -> str:
    return markdown.markdown(text, extensions=["extra"])


def main() -> None:
    en_md, ru_md = SRC.read_text(encoding="utf-8").split("\n---\n", 1)
    page = PAGE_TEMPLATE.format(en=render(en_md), ru=render(ru_md))
    privacy_dir = OUT / "privacy"
    privacy_dir.mkdir(parents=True, exist_ok=True)
    (privacy_dir / "index.html").write_text(page, encoding="utf-8")
    (OUT / "index.html").write_text(REDIRECT_TEMPLATE, encoding="utf-8")
    print(f"built: {privacy_dir / 'index.html'}")


if __name__ == "__main__":
    main()
