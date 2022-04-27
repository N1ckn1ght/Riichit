from PIL import Image
import numpy as np


def ccrop(tile, shape):
    tile_alpha = Image.open(r'.\\tile_alpha.png')
    tile_mask = Image.open(r'.\\tile_mask.png').convert('L')
    tile = Image.composite(tile, tile_alpha, tile_mask)
    
    tile_corner = Image.open(r'.\\tile_corner.png')
    tile.paste(tile_corner, (0, 0), tile_corner)
    
    return tile


source = Image.open(r'.\\ExampleRegular.png')
path_save = ".\\tiles\\"

offset_left = 60
offset_top = 8
add_left = 273
add_top = 217
left = 150
top = 200

groups = ["man", "pin", "sou", "wind", "dragon"]
winds = ["east", "south", "west", "north"]
dragons = ["red", "white", "green"]

for tm in range(3):
    for i in range(5):
        l = offset_left + add_left * i
        t = offset_top + add_top * tm
        temp = source.crop((l, t, l + left, t + top))
        temp = ccrop(temp, (top, left))
        fname = path_save + str(groups[tm]) + "_" + str(i + 1) + ".png"
        temp.save(fname)
        
    for i in range(1):
        l = offset_left + add_left * 5
        t = offset_top + add_top * tm
        temp = source.crop((l, t, l + left, t + top))
        temp = ccrop(temp, (top, left))
        fname = path_save + str(groups[tm]) + "_5_dora.png"
        temp.save(fname)

    for i in range(5, 9):
        l = offset_left + add_left * (i + 1)
        t = offset_top + add_top * tm
        temp = source.crop((l, t, l + left, t + top))
        temp = ccrop(temp, (top, left))
        fname = path_save + str(groups[tm]) + "_" + str(i + 1) + ".png"
        temp.save(fname)

for i in range(4):
    l = offset_left + add_left * i
    t = offset_top + add_top * 3
    temp = source.crop((l, t, l + left, t + top))
    temp = ccrop(temp, (top, left))
    fname = path_save + "wind_" + str(winds[i]) + ".png"
    temp.save(fname)

for i in range(4, 7):
    l = offset_left + add_left * i
    t = offset_top + add_top * 3
    temp = source.crop((l, t, l + left, t + top))
    temp = ccrop(temp, (top, left))
    fname = path_save + "dragon_" + str(dragons[i - 4])  + ".png"
    temp.save(fname)
