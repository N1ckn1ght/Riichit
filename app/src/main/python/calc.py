from mahjong.hand_calculating.hand import HandCalculator
from mahjong.tile import TilesConverter
from mahjong.hand_calculating.hand_config import HandConfig
from mahjong.meld import Meld
from mahjong.constants import EAST
from mahjong.shanten import Shanten


def calc(jhand, jtsumo, jkans, jindicators, jbooleans):
    hand = [int(x) for x in jhand]
    # exception
    # Caused by: com.chaquo.python.PyException: TypeError: 'ArrayList' object is not iterable
    tsumo = int(jtsumo)
    kans = [int(x) for x in jkans]
    indicators = [int(x) for x in jindicators]
    booleans = [bool(x) for x in jbooleans]
    
    hand.append(tsumo)
    return hand
    if (kans is not None):
        for i in kans:
            for j in range(3):
                hand.append(i)
    hand.sort()
    kans.sort()
    man = ''
    pin = ''
    sou = ''
    honors = ''
    melds = []
    mand = ''
    pind = ''
    soud = ''
    honorsd = ''
    for i in hand:
        tile = i // 4
        if (tile > 26):
            honors = honors + str(tile - 26)
        elif (tile > 17):
            sou = sou + str(tile - 17)
        elif (tile > 8):
            pin = pin + str(tile - 8)
        else:
            man = man + str(tile + 1)
    for i in indicators:
        tile = i // 4
        if (tile > 26):
            honorsd = honorsd + str(tile - 26)
        elif (tile > 17):
            soud = soud + str(tile - 17)
        elif (tile > 8):
            pind = pind + str(tile - 8)
        else:
            mand = mand + str(tile + 1)
    for i in kans:
        tile = i // 4
        if (tile > 26):
            melds.append(Meld(meld_type=Meld.KAN, tiles=TilesConverter.string_to_136_array(honors=(str(tile - 26) * 4)), opened=False))
        elif (tile > 17):
            melds.append(Meld(meld_type=Meld.KAN, tiles=TilesConverter.string_to_136_array(sou=(str(tile - 26) * 4)), opened=False))
        elif (tile > 8):
            melds.append(Meld(meld_type=Meld.KAN, tiles=TilesConverter.string_to_136_array(pin=(str(tile - 26) * 4)), opened=False))
        else:
            melds.append(Meld(meld_type=Meld.KAN, tiles=TilesConverter.string_to_136_array(man=(str(tile - 26) * 4)), opened=False))
    win_tile = None
    tile = tsumo // 4
    if (tile > 26):
        win_tile = TilesConverter.string_to_136_array(honors=(str(tile - 26)))[0]
    elif (tile > 17):
        win_tile = TilesConverter.string_to_136_array(sou=(str(tile - 17)))[0]
    elif (tile > 8):
        win_tile = TilesConverter.string_to_136_array(pin=(str(tile - 8)))[0]
    else:
        win_tile = TilesConverter.string_to_136_array(man=(str(tile + 1)))[0]

    calculator = HandCalculator()
    tiles = TilesConverter.string_to_136_array(man=man, pin=pin, sou=sou, honors=honors)
    result = calculator.estimate_hand_value(tiles, win_tile=win_tile, melds=melds,
                                      dora_indicators=TilesConverter.string_to_136_array(man=mand, pin=pind, sou=soud, honors=honorsd),
                                      config=HandConfig(is_riichi=booleans[0],
                                                        is_tsumo=True,
                                                        is_ippatsu=booleans[1],
                                                        is_rinshan=booleans[2],
                                                        is_chankan=False,
                                                        is_haitei=booleans[3],
                                                        is_houtei=False,
                                                        is_nagashi_mangan=False,
                                                        is_tenhou=booleans[4],
                                                        is_renhou=False,
                                                        is_chiihou=False,
                                                        player_wind=None,
                                                        round_wind=EAST))

    arr = []
    if (result.error == 'Hand is not winning'):
        shanten = Shanten()
        result = shanten.calculate_shanten(tiles)
        arr.append(result)
    elif (result.error is None):
        arr.append(result.han)
        arr.append(result.fu)
        arr.append(result.cost['main'] * 3)
        arr.append(result.yaku)
        arr.append(result.fu_details)
    return arr
