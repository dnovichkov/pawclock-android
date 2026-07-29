# -*- coding: utf-8 -*-
"""PawClock store asset generator.

Design: paw mark where the main pad is a clock face (ring + hands at ~10:10),
brand teal palette from core/designsystem (seed #00B3A3, Primary #006A60).
Everything is drawn at 4x supersampling and downscaled with LANCZOS.
"""
from PIL import Image, ImageDraw, ImageFont
import os

OUT = os.path.join(os.path.dirname(__file__), "assets")
TEAL = (0, 106, 96, 255)          # Primary #006A60
TEAL_DARK = (0, 55, 49, 255)      # OnPrimaryDark-ish for depth
MINT = (116, 248, 229, 255)       # PrimaryContainer #74F8E5
WHITE = (255, 255, 255, 255)

SS = 4  # supersampling factor


def draw_paw_clock(draw, cx, cy, s, color=WHITE, ring_scale=1.0):
    """Paw mark centered at (cx, cy); s = mark box size (height units)."""
    # Main pad = clock face ring.
    R = 0.30 * s
    ring = 0.085 * s * ring_scale
    ccx, ccy = cx, cy + 0.13 * s
    draw.ellipse([ccx - R, ccy - R, ccx + R, ccy + R], outline=color, width=round(ring))
    # Hands (~10:10), round caps via line + end dots.
    hw = 0.055 * s
    for (dx, dy, w) in [(-0.115 * s, -0.105 * s, hw), (0.105 * s, -0.135 * s, hw * 0.85)]:
        draw.line([ccx, ccy, ccx + dx, ccy + dy], fill=color, width=round(w))
        r = w / 2
        for (px, py) in [(ccx, ccy), (ccx + dx, ccy + dy)]:
            draw.ellipse([px - r, py - r, px + r, py + r], fill=color)
    # Toes: inner pair higher, outer pair lower.
    toes = [
        (cx - 0.145 * s, cy - 0.27 * s, 0.088 * s, 0.118 * s),
        (cx + 0.145 * s, cy - 0.27 * s, 0.088 * s, 0.118 * s),
        (cx - 0.345 * s, cy - 0.155 * s, 0.077 * s, 0.101 * s),
        (cx + 0.345 * s, cy - 0.155 * s, 0.077 * s, 0.101 * s),
    ]
    for (tx, ty, rx, ry) in toes:
        draw.ellipse([tx - rx, ty - ry, tx + rx, ty + ry], fill=color)


def rounded_rect_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m


def make_icon(size, shape):
    """shape: 'square' (full bleed, store), 'rounded' (legacy), 'circle' (legacy round)."""
    S = size * SS
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Background with subtle vertical gradient (teal -> slightly darker).
    bg = Image.new("RGBA", (S, S))
    bd = ImageDraw.Draw(bg)
    for y in range(S):
        t = y / S
        c = tuple(round(TEAL[i] * (1 - t) + (TEAL[i] * 0.72 + TEAL_DARK[i] * 0.28) * t) for i in range(3)) + (255,)
        bd.line([(0, y), (S, y)], fill=c)
    if shape == "square":
        img = bg
    else:
        pad = 0 if shape == "circle" else 0
        mask = Image.new("L", (S, S), 0)
        md = ImageDraw.Draw(mask)
        if shape == "circle":
            md.ellipse([pad, pad, S - 1 - pad, S - 1 - pad], fill=255)
        else:
            md.rounded_rectangle([0, 0, S - 1, S - 1], radius=round(S * 0.22), fill=255)
        img.paste(bg, (0, 0), mask)
    d = ImageDraw.Draw(img)
    draw_paw_clock(d, S / 2, S * 0.52, S * 0.62)
    return img.resize((size, size), Image.LANCZOS)


def load_font(names, px):
    for n in names:
        p = os.path.join("C:/Windows/Fonts", n)
        if os.path.exists(p):
            return ImageFont.truetype(p, px)
    return ImageFont.load_default()


def make_feature_graphic(lang):
    W, H = 1024, 500
    S = SS
    img = Image.new("RGBA", (W * S, H * S))
    d = ImageDraw.Draw(img)
    # Diagonal-ish gradient background.
    for y in range(H * S):
        t = y / (H * S)
        c = tuple(round(TEAL[i] * (1 - t * 0.55) + TEAL_DARK[i] * (t * 0.55) * 0.6) for i in range(3)) + (255,)
        d.line([(0, y), (W * S, y)], fill=c)
    # Big translucent paw watermark on the right.
    wm = Image.new("RGBA", (W * S, H * S), (0, 0, 0, 0))
    wd = ImageDraw.Draw(wm)
    draw_paw_clock(wd, W * S * 0.80, H * S * 0.56, H * S * 0.92, color=(255, 255, 255, 46))
    img = Image.alpha_composite(img, wm)
    d = ImageDraw.Draw(img)
    # Small solid paw badge above the title.
    draw_paw_clock(d, W * S * 0.115, H * S * 0.30, H * S * 0.30, color=MINT)
    title_f = load_font(["segoeuib.ttf", "arialbd.ttf"], round(118 * S))
    tag_f = load_font(["segoeui.ttf", "arial.ttf"], round(41 * S))
    sub_f = load_font(["segoeui.ttf", "arial.ttf"], round(30 * S))
    d.text((W * S * 0.205, H * S * 0.155), "PawClock", font=title_f, fill=WHITE)
    if lang == "ru":
        tagline = "Возраст питомца в человеческих годах"
        sub = "12 видов животных · научные формулы · офлайн и без рекламы"
    else:
        tagline = "Your pet's age in human years"
        sub = "12 species · science-based formulas · offline, ad-free"
    d.text((W * S * 0.09, H * S * 0.60), tagline, font=tag_f, fill=WHITE)
    d.text((W * S * 0.09, H * S * 0.74), sub, font=sub_f, fill=MINT)
    return img.resize((W, H), Image.LANCZOS).convert("RGB")


def main():
    os.makedirs(OUT, exist_ok=True)
    # Play/RuStore icon: full-bleed 512 square (stores apply their own mask).
    make_icon(512, "square").convert("RGB").save(os.path.join(OUT, "icon_512.png"))
    # Legacy launcher mipmaps.
    for dpi, px in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
        make_icon(px, "rounded").save(os.path.join(OUT, f"ic_launcher_{dpi}.png"))
        make_icon(px, "circle").save(os.path.join(OUT, f"ic_launcher_round_{dpi}.png"))
    # Feature graphics.
    make_feature_graphic("ru").save(os.path.join(OUT, "feature_ru.png"))
    make_feature_graphic("en").save(os.path.join(OUT, "feature_en.png"))
    # Preview collage for quick visual check.
    prev = Image.new("RGB", (1024, 1240), (240, 240, 240))
    prev.paste(make_icon(512, "square").convert("RGB"), (20, 20))
    prev.paste(make_icon(192, "rounded"), (560, 20), make_icon(192, "rounded"))
    prev.paste(make_icon(192, "circle"), (560, 240), make_icon(192, "circle"))
    prev.paste(Image.open(os.path.join(OUT, "feature_ru.png")), (0, 560))
    prev.save(os.path.join(OUT, "preview.png"))
    print("done:", os.listdir(OUT))


if __name__ == "__main__":
    main()
