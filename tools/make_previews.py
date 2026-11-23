#!/usr/bin/env python3
"""Vistas previas ilustradas del diseño de la app (PNG puro, sin librerías)."""
import math
import os
import struct
import zlib

W, H = 520, 1040
ROOT = os.path.dirname(os.path.dirname(__file__))
OUT_DIR = os.path.join(ROOT, "docs", "screenshots")

FONT = {
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "B": ["11110", "10001", "10001", "11110", "10001", "10001", "11110"],
    "C": ["01111", "10000", "10000", "10000", "10000", "10000", "01111"],
    "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
    "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    "G": ["01111", "10000", "10000", "10111", "10001", "10001", "01111"],
    "H": ["10001", "10001", "10001", "11111", "10001", "10001", "10001"],
    "I": ["11111", "00100", "00100", "00100", "00100", "00100", "11111"],
    "K": ["10001", "10010", "10100", "11000", "10100", "10010", "10001"],
    "L": ["10000", "10000", "10000", "10000", "10000", "10000", "11111"],
    "M": ["10001", "11011", "10101", "10101", "10001", "10001", "10001"],
    "N": ["10001", "11001", "10101", "10011", "10001", "10001", "10001"],
    "O": ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
    "P": ["11110", "10001", "10001", "11110", "10000", "10000", "10000"],
    "R": ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
    "S": ["01111", "10000", "10000", "01110", "00001", "00001", "11110"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
    "U": ["10001", "10001", "10001", "10001", "10001", "10001", "01110"],
    "V": ["10001", "10001", "10001", "10001", "10001", "01010", "00100"],
    "0": ["01110", "10001", "10011", "10101", "11001", "10001", "01110"],
    "1": ["00100", "01100", "00100", "00100", "00100", "00100", "01110"],
    "2": ["01110", "10001", "00001", "00010", "00100", "01000", "11111"],
    "3": ["11110", "00001", "00001", "01110", "00001", "00001", "11110"],
    "4": ["00010", "00110", "01010", "10010", "11111", "00010", "00010"],
    "5": ["11111", "10000", "10000", "11110", "00001", "00001", "11110"],
    "6": ["01110", "10000", "10000", "11110", "10001", "10001", "01110"],
    "7": ["11111", "00001", "00010", "00100", "01000", "01000", "01000"],
    "8": ["01110", "10001", "10001", "01110", "10001", "10001", "01110"],
    "9": ["01110", "10001", "10001", "01111", "00001", "00001", "01110"],
    " ": ["00000"] * 7,
    ".": ["00000", "00000", "00000", "00000", "00000", "01100", "01100"],
    ",": ["00000", "00000", "00000", "00000", "01100", "00100", "01000"],
    "%": ["11001", "11010", "00010", "00100", "01000", "01011", "10011"],
    "+": ["00000", "00100", "00100", "11111", "00100", "00100", "00000"],
    "-": ["00000", "00000", "00000", "11111", "00000", "00000", "00000"],
    "=": ["00000", "11111", "00000", "11111", "00000", "00000", "00000"],
}

PINK = (255, 111, 169)
PURPLE = (155, 109, 255)
WHITE = (255, 255, 255)
MUTED = (195, 178, 230)


class Canvas:
    def __init__(self):
        self.px = bytearray(W * H * 3)
        for y in range(H):
            for x in range(W):
                t = 0.35 * x / W + 0.65 * y / H
                stops = [(0, (17, 12, 34)), (0.5, (42, 23, 72)), (1, (15, 12, 28))]
                for i in range(len(stops) - 1):
                    a, ca = stops[i]
                    b, cb = stops[i + 1]
                    if a <= t <= b:
                        k = (t - a) / (b - a)
                        self.put(x, y, tuple(int(ca[j] + (cb[j] - ca[j]) * k) for j in range(3)))
                        break

    def put(self, x, y, color, alpha=1.0):
        x, y = int(x), int(y)
        if not (0 <= x < W and 0 <= y < H) or alpha <= 0:
            return
        i = (y * W + x) * 3
        for k in range(3):
            self.px[i + k] = int(self.px[i + k] * (1 - alpha) + color[k] * alpha)

    def circle(self, cx, cy, r, color, alpha=1.0):
        for y in range(max(0, int(cy - r)), min(H, int(cy + r) + 1)):
            for x in range(max(0, int(cx - r)), min(W, int(cx + r) + 1)):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                    self.put(x, y, color, alpha)

    def rect(self, x, y, w, h, color, radius=0, alpha=1.0, border=None):
        for yy in range(max(0, int(y)), min(H, int(y + h))):
            for xx in range(max(0, int(x)), min(W, int(x + w))):
                dx = max(x - xx, 0, xx - (x + w - 1))
                dy = max(y - yy, 0, yy - (y + h - 1))
                if math.hypot(dx, dy) <= radius:
                    self.put(xx, yy, color, alpha)
        if border:
            for d in range(2):
                self.rect(x + d, y + d, w - 2 * d, 2, border, 1)
                self.rect(x + d, y + h - 2 - d, w - 2 * d, 2, border, 1)
                self.rect(x + d, y + d, 2, h - 2 * d, border, 1)
                self.rect(x + w - 2 - d, y + d, 2, h - 2 * d, border, 1)

    def text(self, message, x, y, scale, color, glow=False):
        cursor = x
        for ch in message.upper():
            for row, bits in enumerate(FONT.get(ch, FONT[" "])):
                for col, bit in enumerate(bits):
                    if bit != "1":
                        continue
                    gx, gy = cursor + col * scale, y + row * scale
                    if glow:
                        for oy in (-2, 0, 2):
                            for ox in (-2, 0, 2):
                                self.put(gx + ox, gy + oy, PINK, 0.18)
                    for dy in range(scale):
                        for dx in range(scale):
                            self.put(gx + dx, gy + dy, color)
            cursor += 6 * scale

    def centered(self, message, cx, y, scale, color):
        width = len(message) * 6 * scale - scale
        self.text(message, int(cx - width / 2), y, scale, color)

    def save(self, path):
        raw = b"".join(b"\x00" + bytes(self.px[y * W * 3:(y + 1) * W * 3]) for y in range(H))

        def chunk(tag, data):
            return (struct.pack(">I", len(data)) + tag + data
                    + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 2, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(raw, 9))
        png += chunk(b"IEND", b"")
        with open(path, "wb") as f:
            f.write(png)


def header(c, active):
    c.circle(78, 78, 40, PURPLE, 0.35)
    c.circle(78, 82, 30, (181, 123, 255))
    c.circle(78, 88, 23, (255, 227, 211))
    c.rect(55, 66, 46, 16, (255, 154, 200), 8)
    c.circle(70, 88, 4, (59, 34, 84))
    c.circle(86, 88, 4, (59, 34, 84))
    c.circle(73, 85, 1, WHITE)
    c.circle(89, 85, 1, WHITE)
    c.text("ANIME CALC", 134, 46, 4, WHITE, True)
    c.text("KAWAII  EDITION", 136, 88, 2, MUTED)
    c.rect(24, 136, 230, 46, (69, 47, 121) if active else (40, 30, 70), 16,
           border=PINK if active else (90, 70, 130))
    c.centered("CALC", 139, 150, 2, WHITE if active else MUTED)
    c.rect(266, 136, 230, 46, (69, 47, 121) if not active else (40, 30, 70), 16,
           border=PINK if not active else (90, 70, 130))
    c.centered("ANIME", 381, 150, 2, WHITE if not active else MUTED)


def calculator():
    c = Canvas()
    header(c, True)
    c.rect(24, 206, 472, 178, (33, 26, 58), 22, border=PURPLE)
    c.text("7 + 35", 450, 226, 2, MUTED)
    c.text("42", 400, 270, 9, WHITE)
    keys = [
        ["C", "%", "+"],
        ["7", "8", "9"],
        ["4", "5", "6"],
        ["1", "2", "3"],
    ]
    x0, y0, cw, ch, gap = 24, 410, 112, 104, 8
    rows = [["C", "-", "%", "/"], ["7", "8", "9", "*"], ["4", "5", "6", "-"],
            ["1", "2", "3", "+"], ["=", "0", ".", "="]]
    for r, row in enumerate(rows):
        for col, label in enumerate(row):
            x = x0 + col * (cw + gap)
            y = y0 + r * (ch + gap)
            is_eq = label == "=" and r == 4 and col == 3
            c.rect(x, y, cw, ch, PINK if is_eq else (46, 38, 80), 16,
                   border=PINK if is_eq else (120, 95, 180))
            c.centered(label, x + cw / 2, y + ch / 2 - 10, 4 if len(label) == 1 else 3,
                       (20, 15, 35) if is_eq else WHITE)
    c.save(os.path.join(OUT_DIR, "calculator.png"))


def anime():
    c = Canvas()
    header(c, False)
    c.rect(24, 206, 472, 286, (33, 26, 58), 22, border=(80, 60, 120))
    c.text("PROGRESO", 44, 226, 3, PINK)
    for i in range(3):
        c.rect(44, 270 + i * 52, 432, 42, (46, 38, 80), 10, border=PURPLE)
    c.text("2h 0m  41%", 44, 442, 3, (220, 205, 255))

    c.rect(24, 510, 472, 222, (33, 26, 58), 22, border=(80, 60, 120))
    c.text("NOTAS", 44, 530, 3, PINK)
    c.rect(44, 574, 432, 42, (46, 38, 80), 10, border=PURPLE)
    c.text("8,33  3 NOTAS", 44, 646, 3, (220, 205, 255))

    c.rect(24, 750, 472, 250, (33, 26, 58), 22, border=(80, 60, 120))
    c.text("GITHUB", 44, 770, 3, PINK)
    c.rect(44, 816, 432, 44, (69, 47, 121), 12, border=PINK)
    c.centered("UPDATE", 260, 828, 2, WHITE)
    c.rect(44, 876, 432, 44, (69, 47, 121), 12, border=PINK)
    c.centered("OPEN", 260, 888, 2, WHITE)
    c.save(os.path.join(OUT_DIR, "anime.png"))


os.makedirs(OUT_DIR, exist_ok=True)
calculator()
anime()
for name in ("calculator.png", "anime.png"):
    path = os.path.join(OUT_DIR, name)
    print("Generado", path, os.path.getsize(path), "bytes")
