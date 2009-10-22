/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
// Source File Name:   UniCharacter.java

package comx.Tiger.util;


// Referenced classes of package com.wintertree.util:
//      Search

public class UniCharacter {

  public static final char bom = 65279;
  private static final byte A_ = 1;
  private static final byte D_ = 2;
  private static final byte P_ = 4;
  private static final byte S_ = 8;
  private static final byte L_ = 16;
  private static final byte U_ = 32;
  private static final byte V_ = 64;
  private static final byte Latin1CharClass[] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0, 8,
    8, 0, 0, 8, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 8, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 4, 4,
    4, 4, 4, 4, 4, 97, 33, 33, 33, 97,
    33, 33, 33, 97, 33, 33, 33, 33, 33, 97,
    33, 33, 33, 33, 33, 97, 33, 33, 33, 33,
    33, 4, 4, 4, 4, 4, 4, 81, 17, 17,
    17, 81, 17, 17, 17, 81, 17, 17, 17, 17,
    17, 81, 17, 17, 17, 17, 17, 81, 17, 17,
    17, 17, 17, 4, 4, 4, 4, 4, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 0, 0, 0,
    0, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 97, 97, 97, 97, 97, 97, 97, 33,
    97, 97, 97, 97, 97, 97, 97, 97, 33, 33,
    97, 97, 97, 97, 97, 4, 97, 97, 97, 97,
    97, 33, 33, 17, 81, 81, 81, 81, 81, 81,
    81, 17, 81, 81, 81, 81, 81, 81, 81, 81,
    81, 17, 81, 81, 81, 81, 81, 4, 81, 81,
    81, 81, 81, 17, 17, 17
  };
  private static final char Latin1ToBase[] = {
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', 'A', 'B', 'C', 'D', 'E',
    'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
    'Z', '\0', '\0', '\0', '\0', '\0', '\0', 'A', 'B', 'C',
    'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
    'X', 'Y', 'Z', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
    '\0', '\0', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'C',
    'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', '\0', 'N',
    'O', 'O', 'O', 'O', 'O', '\0', 'O', 'U', 'U', 'U',
    'U', 'Y', '\0', 'S', 'A', 'A', 'A', 'A', 'A', 'A',
    'A', 'C', 'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I',
    '\0', 'N', 'O', 'O', 'O', 'O', 'O', '\0', 'O', 'U',
    'U', 'U', 'U', 'Y', '\0', 'Y'
  };
  private static final long baseTbl[] = {
    0xaa0041L, 0xb503bcL, 0xba004fL, 0xc00041L, 0xc10041L, 0xc20041L, 0xc30041L, 0xc40041L, 0xc50041L, 0xc70043L,
    0xc80045L, 0xc90045L, 0xca0045L, 0xcb0045L, 0xcc0049L, 0xcd0049L, 0xce0049L, 0xcf0049L, 0xd1004eL, 0xd2004fL,
    0xd3004fL, 0xd4004fL, 0xd5004fL, 0xd6004fL, 0xd90055L, 0xda0055L, 0xdb0055L, 0xdc0055L, 0xdd0059L, 0xe00041L,
    0xe10041L, 0xe20041L, 0xe30041L, 0xe40041L, 0xe50041L, 0xe70043L, 0xe80045L, 0xe90045L, 0xea0045L, 0xeb0045L,
    0xec0049L, 0xed0049L, 0xee0049L, 0xef0049L, 0xf1004eL, 0xf2004fL, 0xf3004fL, 0xf4004fL, 0xf5004fL, 0xf6004fL,
    0xf90055L, 0xfa0055L, 0xfb0055L, 0xfc0055L, 0xfd0059L, 0xff0059L, 0x1000041L, 0x1010041L, 0x1020041L, 0x1030041L,
    0x1040041L, 0x1050041L, 0x1060043L, 0x1070043L, 0x1080043L, 0x1090043L, 0x10a0043L, 0x10b0043L, 0x10c0043L, 0x10d0043L,
    0x10e0044L, 0x10f0044L, 0x1120045L, 0x1130045L, 0x1140045L, 0x1150045L, 0x1160045L, 0x1170045L, 0x1180045L, 0x1190045L,
    0x11a0045L, 0x11b0045L, 0x11c0047L, 0x11d0047L, 0x11e0047L, 0x11f0047L, 0x1200047L, 0x1210047L, 0x1220047L, 0x1230047L,
    0x1240048L, 0x1250048L, 0x1280049L, 0x1290049L, 0x12a0049L, 0x12b0049L, 0x12c0049L, 0x12d0049L, 0x12e0049L, 0x12f0049L,
    0x1300049L, 0x1320049L, 0x1330049L, 0x134004aL, 0x135004aL, 0x136004bL, 0x137004bL, 0x139004cL, 0x13a004cL, 0x13b004cL,
    0x13c004cL, 0x13d004cL, 0x13e004cL, 0x13f004cL, 0x140004cL, 0x143004eL, 0x144004eL, 0x145004eL, 0x146004eL, 0x147004eL,
    0x148004eL, 0x14902bcL, 0x14c004fL, 0x14d004fL, 0x14e004fL, 0x14f004fL, 0x150004fL, 0x151004fL, 0x1540052L, 0x1550052L,
    0x1560052L, 0x1570052L, 0x1580052L, 0x1590052L, 0x15a0053L, 0x15b0053L, 0x15c0053L, 0x15d0053L, 0x15e0053L, 0x15f0053L,
    0x1600053L, 0x1610053L, 0x1620054L, 0x1630054L, 0x1640054L, 0x1650054L, 0x1680055L, 0x1690055L, 0x16a0055L, 0x16b0055L,
    0x16c0055L, 0x16d0055L, 0x16e0055L, 0x16f0055L, 0x1700055L, 0x1710055L, 0x1720055L, 0x1730055L, 0x1740057L, 0x1750057L,
    0x1760059L, 0x1770059L, 0x1780059L, 0x179005aL, 0x17a005aL, 0x17b005aL, 0x17c005aL, 0x17d005aL, 0x17e005aL, 0x17f0053L,
    0x1a0004fL, 0x1a1004fL, 0x1af0055L, 0x1b00055L, 0x1c40044L, 0x1c50044L, 0x1c60044L, 0x1c7004cL, 0x1c8004cL, 0x1c9004cL,
    0x1ca004eL, 0x1cb004eL, 0x1cc004eL, 0x1cd0041L, 0x1ce0041L, 0x1cf0049L, 0x1d00049L, 0x1d1004fL, 0x1d2004fL, 0x1d30055L,
    0x1d40055L, 0x1d500dcL, 0x1d600fcL, 0x1d700dcL, 0x1d800fcL, 0x1d900dcL, 0x1da00fcL, 0x1db00dcL, 0x1dc00fcL, 0x1de00c4L,
    0x1df00e4L, 0x1e00041L, 0x1e10041L, 0x1e200c6L, 0x1e300e6L, 0x1e60047L, 0x1e70047L, 0x1e8004bL, 0x1e9004bL, 0x1ea004fL,
    0x1eb004fL, 0x1ec01eaL, 0x1ed01ebL, 0x1ee01b7L, 0x1ef0292L, 0x1f0005aL, 0x1f10044L, 0x1f20044L, 0x1f30044L, 0x1f40047L,
    0x1f50047L, 0x1fa00c5L, 0x1fb00e5L, 0x1fc00c6L, 0x1fd00e6L, 0x1fe00d8L, 0x1ff00f8L, 0x2000041L, 0x2010041L, 0x2020041L,
    0x2030041L, 0x2040045L, 0x2050045L, 0x2060045L, 0x2070045L, 0x2080049L, 0x2090049L, 0x20a0049L, 0x20b0049L, 0x20c004fL,
    0x20d004fL, 0x20e004fL, 0x20f004fL, 0x2100052L, 0x2110052L, 0x2120052L, 0x2130052L, 0x2140055L, 0x2150055L, 0x2160055L,
    0x2170055L, 0x3860391L, 0x3880395L, 0x3890397L, 0x38a0399L, 0x38c039fL, 0x38e03a5L, 0x38f03a9L, 0x39003b9L, 0x3aa0399L,
    0x3ab03a5L, 0x3ac03b1L, 0x3ad03b5L, 0x3ae03b7L, 0x3af03b9L, 0x3b003c5L, 0x3ca03b9L, 0x3cb03c5L, 0x3cc03bfL, 0x3cd03c5L,
    0x3ce03c9L, 0x3d303d2L, 0x3d403d2L, 0x4010415L, 0x4030413L, 0x4070406L, 0x40c041aL, 0x40e0423L, 0x4190418L, 0x4390438L,
    0x4510435L, 0x4530433L, 0x4570456L, 0x45c043aL, 0x45e0443L, 0x4760474L, 0x4770475L, 0x4c10416L, 0x4c20436L, 0x4d00410L,
    0x4d10430L, 0x4d20410L, 0x4d30430L, 0x4d400c6L, 0x4d500e6L, 0x4d60415L, 0x4d70435L, 0x4d8018fL, 0x4d90259L, 0x4da018fL,
    0x4db0259L, 0x4dc0416L, 0x4dd0436L, 0x4de0417L, 0x4df0437L, 0x4e001b7L, 0x4e10292L, 0x4e20418L, 0x4e30438L, 0x4e40418L,
    0x4e50438L, 0x4e6041eL, 0x4e7043eL, 0x4e8019fL, 0x4e90275L, 0x4ea019fL, 0x4eb0275L, 0x4ee0423L, 0x4ef0443L, 0x4f00423L,
    0x4f10443L, 0x4f20423L, 0x4f30443L, 0x4f40427L, 0x4f50447L, 0x4f8042bL, 0x4f9044bL, 0x5870565L, 0x9290928L, 0x9310930L,
    0x9340933L, 0x9580915L, 0x9590916L, 0x95a0917L, 0x95b091cL, 0x95c0921L, 0x95d0922L, 0x95e092bL, 0x95f092fL, 0x9b009acL,
    0x9dc09a1L, 0x9dd09a2L, 0x9df09afL, 0xa590a16L, 0xa5a0a17L, 0xa5b0a1cL, 0xa5c0a21L, 0xa5e0a2bL, 0xb5c0b21L, 0xb5d0b22L,
    0xb5f0b2fL, 0xb940b92L, 0xe330e4dL, 0xeb30ecdL, 0xedc0eabL, 0xedd0eabL, 0xf430f42L, 0xf4d0f4cL, 0xf520f51L, 0xf570f56L,
    0xf5c0f5bL, 0xf690f40L, 0x11011100L, 0x11041103L, 0x11081107L, 0x110a1109L, 0x110d110cL, 0x11131102L, 0x11141102L, 0x11151102L,
    0x11161102L, 0x11171103L, 0x11181105L, 0x11191105L, 0x111a1105L, 0x111b1105L, 0x111c1106L, 0x111d1106L, 0x111e1107L, 0x111f1107L,
    0x11201107L, 0x11211107L, 0x11221107L, 0x11231107L, 0x11241107L, 0x11251107L, 0x11261107L, 0x11271107L, 0x11281107L, 0x11291107L,
    0x112a1107L, 0x112b1107L, 0x112c1107L, 0x112d1109L, 0x112e1109L, 0x112f1109L, 0x11301109L, 0x11311109L, 0x11321109L, 0x11331109L,
    0x11341109L, 0x11351109L, 0x11361109L, 0x11371109L, 0x11381109L, 0x11391109L, 0x113a1109L, 0x113b1109L, 0x113d113cL, 0x113f113eL,
    0x1141110bL, 0x1142110bL, 0x1143110bL, 0x1144110bL, 0x1145110bL, 0x1146110bL, 0x1147110bL, 0x1148110bL, 0x1149110bL, 0x114a110bL,
    0x114b110bL, 0x114d110cL, 0x114f114eL, 0x11511150L, 0x1152110eL, 0x1153110eL, 0x11561111L, 0x11571111L, 0x11581112L, 0x11621161L,
    0x11641163L, 0x11661165L, 0x11681167L, 0x116a1169L, 0x116b1169L, 0x116c1169L, 0x116f116eL, 0x1170116eL, 0x1171116eL, 0x11741173L,
    0x11761161L, 0x11771161L, 0x11781163L, 0x11791163L, 0x117a1165L, 0x117b1165L, 0x117c1165L, 0x117d1167L, 0x117e1167L, 0x117f1169L,
    0x11801169L, 0x11811169L, 0x11821169L, 0x11831169L, 0x1184116dL, 0x1185116dL, 0x1186116dL, 0x1187116dL, 0x1188116dL, 0x1189116eL,
    0x118a116eL, 0x118b116eL, 0x118c116eL, 0x118d116eL, 0x118e1172L, 0x118f1172L, 0x11901172L, 0x11911172L, 0x11921172L, 0x11931172L,
    0x11941172L, 0x11951173L, 0x11961173L, 0x11971174L, 0x11981175L, 0x11991175L, 0x119a1175L, 0x119b1175L, 0x119c1175L, 0x119d1175L,
    0x119f119eL, 0x11a0119eL, 0x11a1119eL, 0x11a2119eL, 0x11a911a8L, 0x11aa11a8L, 0x11ac11abL, 0x11ad11abL, 0x11b011afL, 0x11b111afL,
    0x11b211afL, 0x11b311afL, 0x11b411afL, 0x11b511afL, 0x11b611afL, 0x11b911b8L, 0x11bb11baL, 0x11c311a8L, 0x11c411a8L, 0x11c511abL,
    0x11c611abL, 0x11c711abL, 0x11c811abL, 0x11c911abL, 0x11ca11aeL, 0x11cb11aeL, 0x11cc11afL, 0x11cd11afL, 0x11ce11afL, 0x11cf11afL,
    0x11d011afL, 0x11d111afL, 0x11d211afL, 0x11d311afL, 0x11d411afL, 0x11d511afL, 0x11d611afL, 0x11d711afL, 0x11d811afL, 0x11d911afL,
    0x11da11b7L, 0x11db11b7L, 0x11dc11b7L, 0x11dd11b7L, 0x11de11b7L, 0x11df11b7L, 0x11e011b7L, 0x11e111b7L, 0x11e211b7L, 0x11e311b8L,
    0x11e411b8L, 0x11e511b8L, 0x11e611b8L, 0x11e711baL, 0x11e811baL, 0x11e911baL, 0x11ea11baL, 0x11ec11bcL, 0x11ed11bcL, 0x11ee11bcL,
    0x11ef11bcL, 0x11f111f0L, 0x11f211f0L, 0x11f311c1L, 0x11f411c1L, 0x11f511c2L, 0x11f611c2L, 0x11f711c2L, 0x11f811c2L, 0x1e000041L,
    0x1e010061L, 0x1e020042L, 0x1e030062L, 0x1e040042L, 0x1e050062L, 0x1e060042L, 0x1e070062L, 0x1e0800c7L, 0x1e0900e7L, 0x1e0a0044L,
    0x1e0b0064L, 0x1e0c0044L, 0x1e0d0064L, 0x1e0e0044L, 0x1e0f0064L, 0x1e100044L, 0x1e110064L, 0x1e120044L, 0x1e130064L, 0x1e140112L,
    0x1e150113L, 0x1e160112L, 0x1e170113L, 0x1e180045L, 0x1e190065L, 0x1e1a0045L, 0x1e1b0065L, 0x1e1c0114L, 0x1e1d0115L, 0x1e1e0046L,
    0x1e1f0066L, 0x1e200047L, 0x1e210067L, 0x1e220048L, 0x1e230068L, 0x1e240048L, 0x1e250068L, 0x1e260048L, 0x1e270068L, 0x1e280048L,
    0x1e290068L, 0x1e2a0048L, 0x1e2b0068L, 0x1e2c0049L, 0x1e2d0069L, 0x1e2e00cfL, 0x1e2f00efL, 0x1e30004bL, 0x1e31006bL, 0x1e32004bL,
    0x1e33006bL, 0x1e34004bL, 0x1e35006bL, 0x1e36004cL, 0x1e37006cL, 0x1e381e36L, 0x1e391e37L, 0x1e3a004cL, 0x1e3b006cL, 0x1e3c004cL,
    0x1e3d006cL, 0x1e3e004dL, 0x1e3f006dL, 0x1e40004dL, 0x1e41006dL, 0x1e42004dL, 0x1e43006dL, 0x1e44004eL, 0x1e45006eL, 0x1e46004eL,
    0x1e47006eL, 0x1e48004eL, 0x1e49006eL, 0x1e4a004eL, 0x1e4b006eL, 0x1e4c00d5L, 0x1e4d00f5L, 0x1e4e00d5L, 0x1e4f00f5L, 0x1e50014cL,
    0x1e51014dL, 0x1e52014cL, 0x1e53014dL, 0x1e540050L, 0x1e550070L, 0x1e560050L, 0x1e570070L, 0x1e580052L, 0x1e590072L, 0x1e5a0052L,
    0x1e5b0072L, 0x1e5c1e5aL, 0x1e5d1e5bL, 0x1e5e0052L, 0x1e5f0072L, 0x1e600053L, 0x1e610073L, 0x1e620053L, 0x1e630073L, 0x1e64015aL,
    0x1e65015bL, 0x1e660160L, 0x1e670161L, 0x1e681e62L, 0x1e691e63L, 0x1e6a0054L, 0x1e6b0074L, 0x1e6c0054L, 0x1e6d0074L, 0x1e6e0054L,
    0x1e6f0074L, 0x1e700054L, 0x1e710074L, 0x1e720055L, 0x1e730075L, 0x1e740055L, 0x1e750075L, 0x1e760055L, 0x1e770075L, 0x1e780168L,
    0x1e790169L, 0x1e7a016aL, 0x1e7b016bL, 0x1e7c0056L, 0x1e7d0076L, 0x1e7e0056L, 0x1e7f0076L, 0x1e800057L, 0x1e810077L, 0x1e820057L,
    0x1e830077L, 0x1e840057L, 0x1e850077L, 0x1e860057L, 0x1e870077L, 0x1e880057L, 0x1e890077L, 0x1e8a0058L, 0x1e8b0078L, 0x1e8c0058L,
    0x1e8d0078L, 0x1e8e0059L, 0x1e8f0079L, 0x1e90005aL, 0x1e91007aL, 0x1e92005aL, 0x1e93007aL, 0x1e94005aL, 0x1e95007aL, 0x1e960068L,
    0x1e970074L, 0x1e980077L, 0x1e990079L, 0x1e9b017fL, 0x1ea00041L, 0x1ea10061L, 0x1ea20041L, 0x1ea30061L, 0x1ea400c2L, 0x1ea500e2L,
    0x1ea600c2L, 0x1ea700e2L, 0x1ea800c2L, 0x1ea900e2L, 0x1eaa00c2L, 0x1eab00e2L, 0x1eac00c2L, 0x1ead00e2L, 0x1eae0102L, 0x1eaf0103L,
    0x1eb00102L, 0x1eb10103L, 0x1eb20102L, 0x1eb30103L, 0x1eb40102L, 0x1eb50103L, 0x1eb60102L, 0x1eb70103L, 0x1eb80045L, 0x1eb90065L,
    0x1eba0045L, 0x1ebb0065L, 0x1ebc0045L, 0x1ebd0065L, 0x1ebe00caL, 0x1ebf00eaL, 0x1ec000caL, 0x1ec100eaL, 0x1ec200caL, 0x1ec300eaL,
    0x1ec400caL, 0x1ec500eaL, 0x1ec600caL, 0x1ec700eaL, 0x1ec80049L, 0x1ec90069L, 0x1eca0049L, 0x1ecb0069L, 0x1ecc004fL, 0x1ecd006fL,
    0x1ece004fL, 0x1ecf006fL, 0x1ed000d4L, 0x1ed100f4L, 0x1ed200d4L, 0x1ed300f4L, 0x1ed400d4L, 0x1ed500f4L, 0x1ed600d4L, 0x1ed700f4L,
    0x1ed800d4L, 0x1ed900f4L, 0x1eda01a0L, 0x1edb01a1L, 0x1edc01a0L, 0x1edd01a1L, 0x1ede01a0L, 0x1edf01a1L, 0x1ee001a0L, 0x1ee101a1L,
    0x1ee201a0L, 0x1ee301a1L, 0x1ee40055L, 0x1ee50075L, 0x1ee60055L, 0x1ee70075L, 0x1ee801afL, 0x1ee901b0L, 0x1eea01afL, 0x1eeb01b0L,
    0x1eec01afL, 0x1eed01b0L, 0x1eee01afL, 0x1eef01b0L, 0x1ef001afL, 0x1ef101b0L, 0x1ef20059L, 0x1ef30079L, 0x1ef40059L, 0x1ef50079L,
    0x1ef60059L, 0x1ef70079L, 0x1ef80059L, 0x1ef90079L, 0x1f0003b1L, 0x1f0103b1L, 0x1f021f00L, 0x1f031f01L, 0x1f041f00L, 0x1f051f01L,
    0x1f061f00L, 0x1f071f01L, 0x1f080391L, 0x1f090391L, 0x1f0a1f08L, 0x1f0b1f09L, 0x1f0c1f08L, 0x1f0d1f09L, 0x1f0e1f08L, 0x1f0f1f09L,
    0x1f1003b5L, 0x1f1103b5L, 0x1f121f10L, 0x1f131f11L, 0x1f141f10L, 0x1f151f11L, 0x1f180395L, 0x1f190395L, 0x1f1a1f18L, 0x1f1b1f19L,
    0x1f1c1f18L, 0x1f1d1f19L, 0x1f2003b7L, 0x1f2103b7L, 0x1f221f20L, 0x1f231f21L, 0x1f241f20L, 0x1f251f21L, 0x1f261f20L, 0x1f271f21L,
    0x1f280397L, 0x1f290397L, 0x1f2a1f28L, 0x1f2b1f29L, 0x1f2c1f28L, 0x1f2d1f29L, 0x1f2e1f28L, 0x1f2f1f29L, 0x1f3003b9L, 0x1f3103b9L,
    0x1f321f30L, 0x1f331f31L, 0x1f341f30L, 0x1f351f31L, 0x1f361f30L, 0x1f371f31L, 0x1f380399L, 0x1f390399L, 0x1f3a1f38L, 0x1f3b1f39L,
    0x1f3c1f38L, 0x1f3d1f39L, 0x1f3e1f38L, 0x1f3f1f39L, 0x1f4003bfL, 0x1f4103bfL, 0x1f421f40L, 0x1f431f41L, 0x1f441f40L, 0x1f451f41L,
    0x1f48039fL, 0x1f49039fL, 0x1f4a1f48L, 0x1f4b1f49L, 0x1f4c1f48L, 0x1f4d1f49L, 0x1f5003c5L, 0x1f5103c5L, 0x1f521f50L, 0x1f531f51L,
    0x1f541f50L, 0x1f551f51L, 0x1f561f50L, 0x1f571f51L, 0x1f5903a5L, 0x1f5b1f59L, 0x1f5d1f59L, 0x1f5f1f59L, 0x1f6003c9L, 0x1f6103c9L,
    0x1f621f60L, 0x1f631f61L, 0x1f641f60L, 0x1f651f61L, 0x1f661f60L, 0x1f671f61L, 0x1f6803a9L, 0x1f6903a9L, 0x1f6a1f68L, 0x1f6b1f69L,
    0x1f6c1f68L, 0x1f6d1f69L, 0x1f6e1f68L, 0x1f6f1f69L, 0x1f7003b1L, 0x1f7103b1L, 0x1f7203b5L, 0x1f7303b5L, 0x1f7403b7L, 0x1f7503b7L,
    0x1f7603b9L, 0x1f7703b9L, 0x1f7803bfL, 0x1f7903bfL, 0x1f7a03c5L, 0x1f7b03c5L, 0x1f7c03c9L, 0x1f7d03c9L, 0x1f801f00L, 0x1f811f01L,
    0x1f821f02L, 0x1f831f03L, 0x1f841f04L, 0x1f851f05L, 0x1f861f06L, 0x1f871f07L, 0x1f881f08L, 0x1f891f09L, 0x1f8a1f0aL, 0x1f8b1f0bL,
    0x1f8c1f0cL, 0x1f8d1f0dL, 0x1f8e1f0eL, 0x1f8f1f0fL, 0x1f901f20L, 0x1f911f21L, 0x1f921f22L, 0x1f931f23L, 0x1f941f24L, 0x1f951f25L,
    0x1f961f26L, 0x1f971f27L, 0x1f981f28L, 0x1f991f29L, 0x1f9a1f2aL, 0x1f9b1f2bL, 0x1f9c1f2cL, 0x1f9d1f2dL, 0x1f9e1f2eL, 0x1f9f1f2fL,
    0x1fa01f60L, 0x1fa11f61L, 0x1fa21f62L, 0x1fa31f63L, 0x1fa41f64L, 0x1fa51f65L, 0x1fa61f66L, 0x1fa71f67L, 0x1fa81f68L, 0x1fa91f69L,
    0x1faa1f6aL, 0x1fab1f6bL, 0x1fac1f6cL, 0x1fad1f6dL, 0x1fae1f6eL, 0x1faf1f6fL, 0x1fb003b1L, 0x1fb103b1L, 0x1fb21f70L, 0x1fb303b1L,
    0x1fb41f71L, 0x1fb603b1L, 0x1fb71fb6L, 0x1fb80391L, 0x1fb90391L, 0x1fba0391L, 0x1fbb0391L, 0x1fbc0391L, 0x1fbe0399L, 0x1fc21f74L,
    0x1fc303b7L, 0x1fc41f75L, 0x1fc603b7L, 0x1fc71fc6L, 0x1fc80395L, 0x1fc90395L, 0x1fca0397L, 0x1fcb0397L, 0x1fcc0397L, 0x1fd003b9L,
    0x1fd103b9L, 0x1fd203caL, 0x1fd303caL, 0x1fd603b9L, 0x1fd703caL, 0x1fd80399L, 0x1fd90399L, 0x1fda0399L, 0x1fdb0399L, 0x1fe003c5L,
    0x1fe103c5L, 0x1fe203cbL, 0x1fe303cbL, 0x1fe403c1L, 0x1fe503c1L, 0x1fe603c5L, 0x1fe703cbL, 0x1fe803a5L, 0x1fe903a5L, 0x1fea03a5L,
    0x1feb03a5L, 0x1fec03a1L, 0x1ff21f7cL, 0x1ff303c9L, 0x1ff41f79L, 0x1ff603c9L, 0x1ff71ff6L, 0x1ff8039fL, 0x1ff9039fL, 0x1ffa03a9L,
    0x1ffb03a9L, 0x1ffc03a9L, 0x207f006eL, 0x20a80052L, 0x21020043L, 0x21070190L, 0x210a0067L, 0x210b0048L, 0x210c0048L, 0x210d0048L,
    0x210e0068L, 0x210f0127L, 0x21100049L, 0x21110049L, 0x2112004cL, 0x2113006cL, 0x2115004eL, 0x2116004eL, 0x21180050L, 0x21190050L,
    0x211a0051L, 0x211b0052L, 0x211c0052L, 0x211d0052L, 0x21200053L, 0x21210054L, 0x21220054L, 0x2124005aL, 0x212603a9L, 0x2128005aL,
    0x212a004bL, 0x212b00c5L, 0x212c0042L, 0x212d0043L, 0x212f0065L, 0x21300045L, 0x21310046L, 0x2133004dL, 0x2134006fL, 0x213505d0L,
    0x213605d1L, 0x213705d2L, 0x213805d3L, 0x24b60041L, 0x24b70042L, 0x24b80043L, 0x24b90044L, 0x24ba0045L, 0x24bb0046L, 0x24bc0047L,
    0x24bd0048L, 0x24be0049L, 0x24bf004aL, 0x24c0004bL, 0x24c1004cL, 0x24c2004dL, 0x24c3004eL, 0x24c4004fL, 0x24c50050L, 0x24c60051L,
    0x24c70052L, 0x24c80053L, 0x24c90054L, 0x24ca0055L, 0x24cb0056L, 0x24cc0057L, 0x24cd0058L, 0x24ce0059L, 0x24cf005aL, 0x24d00061L,
    0x24d10062L, 0x24d20063L, 0x24d30064L, 0x24d40065L, 0x24d50066L, 0x24d60067L, 0x24d70068L, 0x24d80069L, 0x24d9006aL, 0x24da006bL,
    0x24db006cL, 0x24dc006dL, 0x24dd006eL, 0x24de006fL, 0x24df0070L, 0x24e00071L, 0x24e10072L, 0x24e20073L, 0x24e30074L, 0x24e40075L,
    0x24e50076L, 0x24e60077L, 0x24e70078L, 0x24e80079L, 0x24e9007aL, 0x304c304bL, 0x304e304dL, 0x3050304fL, 0x30523051L, 0x30543053L,
    0x30563055L, 0x30583057L, 0x305a3059L, 0x305c305bL, 0x305e305dL, 0x3060305fL, 0x30623061L, 0x30653064L, 0x30673066L, 0x30693068L,
    0x3070306fL, 0x3071306fL, 0x30733072L, 0x30743072L, 0x30763075L, 0x30773075L, 0x30793078L, 0x307a3078L, 0x307c307bL, 0x307d307bL,
    0x30943046L, 0x30ac30abL, 0x30ae30adL, 0x30b030afL, 0x30b230b1L, 0x30b430b3L, 0x30b630b5L, 0x30b830b7L, 0x30ba30b9L, 0x30bc30bbL,
    0x30be30bdL, 0x30c030bfL, 0x30c230c1L, 0x30c530c4L, 0x30c730c6L, 0x30c930c8L, 0x30d030cfL, 0x30d130cfL, 0x30d330d2L, 0x30d430d2L,
    0x30d630d5L, 0x30d730d5L, 0x30d930d8L, 0x30da30d8L, 0x30dc30dbL, 0x30dd30dbL, 0x30f430a6L, 0x30f730efL, 0x30f830f0L, 0x30f930f1L,
    0x30fa30f2L, 0x31311100L, 0x31321101L, 0x313311aaL, 0x31341102L, 0x313511acL, 0x313611adL, 0x31371103L, 0x31381104L, 0x31391105L,
    0x313a11b0L, 0x313b11b1L, 0x313c11b2L, 0x313d11b3L, 0x313e11b4L, 0x313f11b5L, 0x3140111aL, 0x31411106L, 0x31421107L, 0x31431108L,
    0x31441121L, 0x31451109L, 0x3146110aL, 0x3147110bL, 0x3148110cL, 0x3149110dL, 0x314a110eL, 0x314b110fL, 0x314c1110L, 0x314d1111L,
    0x314e1112L, 0x314f1161L, 0x31501162L, 0x31511163L, 0x31521164L, 0x31531165L, 0x31541166L, 0x31551167L, 0x31561168L, 0x31571169L,
    0x3158116aL, 0x3159116bL, 0x315a116cL, 0x315b116dL, 0x315c116eL, 0x315d116fL, 0x315e1170L, 0x315f1171L, 0x31601172L, 0x31611173L,
    0x31621174L, 0x31631175L, 0x31641160L, 0x31651114L, 0x31661115L, 0x316711c7L, 0x316811c8L, 0x316911ccL, 0x316a11ceL, 0x316b11d3L,
    0x316c11d7L, 0x316d11d9L, 0x316e111cL, 0x316f11ddL, 0x317011dfL, 0x3171111dL, 0x3172111eL, 0x31731120L, 0x31741122L, 0x31751123L,
    0x31761127L, 0x31771129L, 0x3178112bL, 0x3179112cL, 0x317a112dL, 0x317b112eL, 0x317c112fL, 0x317d1132L, 0x317e1136L, 0x317f1140L,
    0x31801147L, 0x3181114cL, 0x318211f1L, 0x318311f2L, 0x31841157L, 0x31851158L, 0x31861159L, 0x31871184L, 0x31881185L, 0x31891188L,
    0x318a1191L, 0x318b1192L, 0x318c1194L, 0x318d119eL, 0x318e11a1L, 0x31924e00L, 0x31934e8cL, 0x31944e09L, 0x319556dbL, 0x31964e0aL,
    0x31974e2dL, 0x31984e0bL, 0x31997532L, 0x319a4e59L, 0x319b4e19L, 0x319c4e01L, 0x319d5929L, 0x319e5730L, 0x319f4ebaL, 0x32601100L,
    0x32611102L, 0x32621103L, 0x32631105L, 0x32641106L, 0x32651107L, 0x32661109L, 0x3267110bL, 0x3268110cL, 0x3269110eL, 0x326a110fL,
    0x326b1110L, 0x326c1111L, 0x326d1112L, 0x326e1100L, 0x326f1102L, 0x32701103L, 0x32711105L, 0x32721106L, 0x32731107L, 0x32741109L,
    0x3275110bL, 0x3276110cL, 0x3277110eL, 0x3278110fL, 0x32791110L, 0x327a1111L, 0x327b1112L, 0x328a6708L, 0x328b706bL, 0x328c6c34L,
    0x328d6728L, 0x328e91d1L, 0x328f571fL, 0x329065e5L, 0x3291682aL, 0x32926709L, 0x3293793eL, 0x3294540dL, 0x32957279L, 0x32968ca1L,
    0x3297795dL, 0x329852b4L, 0x329979d8L, 0x329a7537L, 0x329b5973L, 0x329c9069L, 0x329d512aL, 0x329e5370L, 0x329f6ce8L, 0x32a09805L,
    0x32a14f11L, 0x32a25199L, 0x32a36b63L, 0x32a44e0aL, 0x32a54e2dL, 0x32a64e0bL, 0x32a75de6L, 0x32a853f3L, 0x32a9533bL, 0x32aa5b97L,
    0x32ab5b66L, 0x32ac76e3L, 0x32ad4f01L, 0x32ae8cc7L, 0x32af5354L, 0x32b0591cL, 0x32d030a2L, 0x32d130a4L, 0x32d230a6L, 0x32d330a8L,
    0x32d430aaL, 0x32d530abL, 0x32d630adL, 0x32d730afL, 0x32d830b1L, 0x32d930b3L, 0x32da30b5L, 0x32db30b7L, 0x32dc30b9L, 0x32dd30bbL,
    0x32de30bdL, 0x32df30bfL, 0x32e030c1L, 0x32e130c4L, 0x32e230c6L, 0x32e330c8L, 0x32e430caL, 0x32e530cbL, 0x32e630ccL, 0x32e730cdL,
    0x32e830ceL, 0x32e930cfL, 0x32ea30d2L, 0x32eb30d5L, 0x32ec30d8L, 0x32ed30dbL, 0x32ee30deL, 0x32ef30dfL, 0x32f030e0L, 0x32f130e1L,
    0x32f230e2L, 0x32f330e4L, 0x32f430e6L, 0x32f530e8L, 0x32f630e9L, 0x32f730eaL, 0x32f830ebL, 0x32f930ecL, 0x32fa30edL, 0x32fb30efL,
    0x32fc30f0L, 0x32fd30f1L, 0x32fe30f2L, 0x330030a2L, 0x330130a2L, 0x330230a2L, 0x330330a2L, 0x330430a4L, 0x330530a4L, 0x330630a6L,
    0x330730a8L, 0x330830a8L, 0x330930aaL, 0x330a30aaL, 0x330b30abL, 0x330c30abL, 0x330d30abL, 0x330e30acL, 0x330f30acL, 0x331030aeL,
    0x331130aeL, 0x331230adL, 0x331330aeL, 0x331430adL, 0x331530adL, 0x331630adL, 0x331730adL, 0x331830b0L, 0x331930b0L, 0x331a30afL,
    0x331b30afL, 0x331c30b1L, 0x331d30b3L, 0x331e30b3L, 0x331f30b5L, 0x332030b5L, 0x332130b7L, 0x332230bbL, 0x332330bbL, 0x332430c0L,
    0x332530c7L, 0x332630c9L, 0x332730c8L, 0x332830caL, 0x332930ceL, 0x332a30cfL, 0x332b30d1L, 0x332c30d1L, 0x332d30d0L, 0x332e30d4L,
    0x332f30d4L, 0x333030d4L, 0x333130d3L, 0x333230d5L, 0x333330d5L, 0x333430d6L, 0x333530d5L, 0x333630d8L, 0x333730daL, 0x333830daL,
    0x333930d8L, 0x333a30daL, 0x333b30daL, 0x333c30d9L, 0x333d30ddL, 0x333e30dcL, 0x333f30dbL, 0x334030ddL, 0x334130dbL, 0x334230dbL,
    0x334330deL, 0x334430deL, 0x334530deL, 0x334630deL, 0x334730deL, 0x334830dfL, 0x334930dfL, 0x334a30dfL, 0x334b30e1L, 0x334c30e1L,
    0x334d30e1L, 0x334e30e4L, 0x334f30e4L, 0x335030e6L, 0x335130eaL, 0x335230eaL, 0x335330ebL, 0x335430ebL, 0x335530ecL, 0x335630ecL,
    0x335730efL, 0x33710068L, 0x33720064L, 0x33730041L, 0x33740062L, 0x3375006fL, 0x33760070L, 0x337b5e73L, 0x337c662dL, 0x337d5927L,
    0x337e660eL, 0x337f682aL, 0x33800070L, 0x3381006eL, 0x338203bcL, 0x3383006dL, 0x3384006bL, 0x3385004bL, 0x3386004dL, 0x33870047L,
    0x33880063L, 0x3389006bL, 0x338a0070L, 0x338b006eL, 0x338c03bcL, 0x338d03bcL, 0x338e006dL, 0x338f006bL, 0x33900048L, 0x3391006bL,
    0x3392004dL, 0x33930047L, 0x33940054L, 0x33990066L, 0x339a006eL, 0x339b03bcL, 0x339c006dL, 0x339d0063L, 0x339e006bL, 0x33a90050L,
    0x33aa006bL, 0x33ab004dL, 0x33ac0047L, 0x33ad0072L, 0x33b00070L, 0x33b1006eL, 0x33b203bcL, 0x33b3006dL, 0x33b40070L, 0x33b5006eL,
    0x33b603bcL, 0x33b7006dL, 0x33b8006bL, 0x33b9004dL, 0x33ba0070L, 0x33bb006eL, 0x33bc03bcL, 0x33bd006dL, 0x33be006bL, 0x33bf004dL,
    0x33c0006bL, 0x33c1004dL, 0x33c30042L, 0x33c40063L, 0x33c50063L, 0x33c70043L, 0x33c80064L, 0x33c90047L, 0x33ca0068L, 0x33cb0048L,
    0x33cc0069L, 0x33cd004bL, 0x33ce004bL, 0x33cf006bL, 0x33d0006cL, 0x33d1006cL, 0x33d2006cL, 0x33d3006cL, 0x33d4006dL, 0x33d5006dL,
    0x33d6006dL, 0x33d70050L, 0x33d90050L, 0x33da0050L, 0x33db0073L, 0x33dc0053L, 0x33dd0057L, 0xfffffffffb000066L, 0xfffffffffb010066L, 0xfffffffffb020066L,
    0xfffffffffb030066L, 0xfffffffffb040066L, 0xfffffffffb05017fL, 0xfffffffffb060073L, 0xfffffffffb130574L, 0xfffffffffb140574L, 0xfffffffffb150574L, 0xfffffffffb16057eL, 0xfffffffffb170574L, 0xfffffffffb1f05f2L,
    0xfffffffffb2005e2L, 0xfffffffffb2105d0L, 0xfffffffffb2205d3L, 0xfffffffffb2305d4L, 0xfffffffffb2405dbL, 0xfffffffffb2505dcL, 0xfffffffffb2605ddL, 0xfffffffffb2705e8L, 0xfffffffffb2805eaL, 0xfffffffffb2a05e9L,
    0xfffffffffb2b05e9L, 0xfffffffffb2c05e9L, 0xfffffffffb2d05e9L, 0xfffffffffb2e05d0L, 0xfffffffffb2f05d0L, 0xfffffffffb3005d0L, 0xfffffffffb3105d1L, 0xfffffffffb3205d2L, 0xfffffffffb3305d3L, 0xfffffffffb3405d4L,
    0xfffffffffb3505d5L, 0xfffffffffb3605d6L, 0xfffffffffb3805d8L, 0xfffffffffb3905d9L, 0xfffffffffb3a05daL, 0xfffffffffb3b05dbL, 0xfffffffffb3c05dcL, 0xfffffffffb3e05deL, 0xfffffffffb4005e0L, 0xfffffffffb4105e1L,
    0xfffffffffb4305e3L, 0xfffffffffb4405e4L, 0xfffffffffb4605e6L, 0xfffffffffb4705e7L, 0xfffffffffb4805e8L, 0xfffffffffb4905e9L, 0xfffffffffb4a05eaL, 0xfffffffffb4b05d5L, 0xfffffffffb4c05d1L, 0xfffffffffb4d05dbL,
    0xfffffffffb4e05e4L, 0xfffffffffb4f05d0L, 0xfffffffffb500671L, 0xfffffffffb510671L, 0xfffffffffb52067bL, 0xfffffffffb53067bL, 0xfffffffffb54067bL, 0xfffffffffb55067bL, 0xfffffffffb56067eL, 0xfffffffffb57067eL,
    0xfffffffffb58067eL, 0xfffffffffb59067eL, 0xfffffffffb5a0680L, 0xfffffffffb5b0680L, 0xfffffffffb5c0680L, 0xfffffffffb5d0680L, 0xfffffffffb5e067aL, 0xfffffffffb5f067aL, 0xfffffffffb60067aL, 0xfffffffffb61067aL,
    0xfffffffffb62067fL, 0xfffffffffb63067fL, 0xfffffffffb64067fL, 0xfffffffffb65067fL, 0xfffffffffb660679L, 0xfffffffffb670679L, 0xfffffffffb680679L, 0xfffffffffb690679L, 0xfffffffffb6a06a4L, 0xfffffffffb6b06a4L,
    0xfffffffffb6c06a4L, 0xfffffffffb6d06a4L, 0xfffffffffb6e06a6L, 0xfffffffffb6f06a6L, 0xfffffffffb7006a6L, 0xfffffffffb7106a6L, 0xfffffffffb720684L, 0xfffffffffb730684L, 0xfffffffffb740684L, 0xfffffffffb750684L,
    0xfffffffffb760683L, 0xfffffffffb770683L, 0xfffffffffb780683L, 0xfffffffffb790683L, 0xfffffffffb7a0686L, 0xfffffffffb7b0686L, 0xfffffffffb7c0686L, 0xfffffffffb7d0686L, 0xfffffffffb7e0687L, 0xfffffffffb7f0687L,
    0xfffffffffb800687L, 0xfffffffffb810687L, 0xfffffffffb82068dL, 0xfffffffffb83068dL, 0xfffffffffb84068cL, 0xfffffffffb85068cL, 0xfffffffffb86068eL, 0xfffffffffb87068eL, 0xfffffffffb880688L, 0xfffffffffb890688L,
    0xfffffffffb8a0698L, 0xfffffffffb8b0698L, 0xfffffffffb8c0691L, 0xfffffffffb8d0691L, 0xfffffffffb8e06a9L, 0xfffffffffb8f06a9L, 0xfffffffffb9006a9L, 0xfffffffffb9106a9L, 0xfffffffffb9206afL, 0xfffffffffb9306afL,
    0xfffffffffb9406afL, 0xfffffffffb9506afL, 0xfffffffffb9606b3L, 0xfffffffffb9706b3L, 0xfffffffffb9806b3L, 0xfffffffffb9906b3L, 0xfffffffffb9a06b1L, 0xfffffffffb9b06b1L, 0xfffffffffb9c06b1L, 0xfffffffffb9d06b1L,
    0xfffffffffb9e06baL, 0xfffffffffb9f06baL, 0xfffffffffba006bbL, 0xfffffffffba106bbL, 0xfffffffffba206bbL, 0xfffffffffba306bbL, 0xfffffffffba406c0L, 0xfffffffffba506c0L, 0xfffffffffba606c1L, 0xfffffffffba706c1L,
    0xfffffffffba806c1L, 0xfffffffffba906c1L, 0xfffffffffbaa06beL, 0xfffffffffbab06beL, 0xfffffffffbac06beL, 0xfffffffffbad06beL, 0xfffffffffbae06d2L, 0xfffffffffbaf06d2L, 0xfffffffffbb006d3L, 0xfffffffffbb106d3L,
    0xfffffffffbd306adL, 0xfffffffffbd406adL, 0xfffffffffbd506adL, 0xfffffffffbd606adL, 0xfffffffffbd706c7L, 0xfffffffffbd806c7L, 0xfffffffffbd906c6L, 0xfffffffffbda06c6L, 0xfffffffffbdb06c8L, 0xfffffffffbdc06c8L,
    0xfffffffffbdd0677L, 0xfffffffffbde06cbL, 0xfffffffffbdf06cbL, 0xfffffffffbe006c5L, 0xfffffffffbe106c5L, 0xfffffffffbe206c9L, 0xfffffffffbe306c9L, 0xfffffffffbe406d0L, 0xfffffffffbe506d0L, 0xfffffffffbe606d0L,
    0xfffffffffbe706d0L, 0xfffffffffbea0626L, 0xfffffffffbeb0626L, 0xfffffffffbec0626L, 0xfffffffffbed0626L, 0xfffffffffbee0626L, 0xfffffffffbef0626L, 0xfffffffffbf00626L, 0xfffffffffbf10626L, 0xfffffffffbf20626L,
    0xfffffffffbf30626L, 0xfffffffffbf40626L, 0xfffffffffbf50626L, 0xfffffffffbf60626L, 0xfffffffffbf70626L, 0xfffffffffbf80626L, 0xfffffffffbfc06ccL, 0xfffffffffbfd06ccL, 0xfffffffffbfe06ccL, 0xfffffffffbff06ccL,
    0xfffffffffc000626L, 0xfffffffffc010626L, 0xfffffffffc020626L, 0xfffffffffc030626L, 0xfffffffffc040626L, 0xfffffffffc050628L, 0xfffffffffc060628L, 0xfffffffffc070628L, 0xfffffffffc080628L, 0xfffffffffc090628L,
    0xfffffffffc0a0628L, 0xfffffffffc0b062aL, 0xfffffffffc0c062aL, 0xfffffffffc0d062aL, 0xfffffffffc0e062aL, 0xfffffffffc0f062aL, 0xfffffffffc10062aL, 0xfffffffffc11062bL, 0xfffffffffc12062bL, 0xfffffffffc13062bL,
    0xfffffffffc14062bL, 0xfffffffffc15062cL, 0xfffffffffc16062cL, 0xfffffffffc17062dL, 0xfffffffffc18062dL, 0xfffffffffc19062eL, 0xfffffffffc1a062eL, 0xfffffffffc1b062eL, 0xfffffffffc1c0633L, 0xfffffffffc1d0633L,
    0xfffffffffc1e0633L, 0xfffffffffc1f0633L, 0xfffffffffc200635L, 0xfffffffffc210635L, 0xfffffffffc220636L, 0xfffffffffc230636L, 0xfffffffffc240636L, 0xfffffffffc250636L, 0xfffffffffc260637L, 0xfffffffffc270637L,
    0xfffffffffc280638L, 0xfffffffffc290639L, 0xfffffffffc2a0639L, 0xfffffffffc2b063aL, 0xfffffffffc2c063aL, 0xfffffffffc2d0641L, 0xfffffffffc2e0641L, 0xfffffffffc2f0641L, 0xfffffffffc300641L, 0xfffffffffc310641L,
    0xfffffffffc320641L, 0xfffffffffc330642L, 0xfffffffffc340642L, 0xfffffffffc350642L, 0xfffffffffc360642L, 0xfffffffffc370643L, 0xfffffffffc380643L, 0xfffffffffc390643L, 0xfffffffffc3a0643L, 0xfffffffffc3b0643L,
    0xfffffffffc3c0643L, 0xfffffffffc3d0643L, 0xfffffffffc3e0643L, 0xfffffffffc3f0644L, 0xfffffffffc400644L, 0xfffffffffc410644L, 0xfffffffffc420644L, 0xfffffffffc430644L, 0xfffffffffc440644L, 0xfffffffffc450645L,
    0xfffffffffc460645L, 0xfffffffffc470645L, 0xfffffffffc480645L, 0xfffffffffc490645L, 0xfffffffffc4a0645L, 0xfffffffffc4b0646L, 0xfffffffffc4c0646L, 0xfffffffffc4d0646L, 0xfffffffffc4e0646L, 0xfffffffffc4f0646L,
    0xfffffffffc500646L, 0xfffffffffc510647L, 0xfffffffffc520647L, 0xfffffffffc530647L, 0xfffffffffc540647L, 0xfffffffffc55064aL, 0xfffffffffc56064aL, 0xfffffffffc57064aL, 0xfffffffffc58064aL, 0xfffffffffc59064aL,
    0xfffffffffc5a064aL, 0xfffffffffc5b0630L, 0xfffffffffc5c0631L, 0xfffffffffc5d0649L, 0xfffffffffc5e0020L, 0xfffffffffc5f0020L, 0xfffffffffc600020L, 0xfffffffffc610020L, 0xfffffffffc620020L, 0xfffffffffc630020L,
    0xfffffffffc640626L, 0xfffffffffc650626L, 0xfffffffffc660626L, 0xfffffffffc670626L, 0xfffffffffc680626L, 0xfffffffffc690626L, 0xfffffffffc6a0628L, 0xfffffffffc6b0628L, 0xfffffffffc6c0628L, 0xfffffffffc6d0628L,
    0xfffffffffc6e0628L, 0xfffffffffc6f0628L, 0xfffffffffc70062aL, 0xfffffffffc71062aL, 0xfffffffffc72062aL, 0xfffffffffc73062aL, 0xfffffffffc74062aL, 0xfffffffffc75062aL, 0xfffffffffc76062bL, 0xfffffffffc77062bL,
    0xfffffffffc78062bL, 0xfffffffffc79062bL, 0xfffffffffc7a062bL, 0xfffffffffc7b062bL, 0xfffffffffc7c0641L, 0xfffffffffc7d0641L, 0xfffffffffc7e0642L, 0xfffffffffc7f0642L, 0xfffffffffc800643L, 0xfffffffffc810643L,
    0xfffffffffc820643L, 0xfffffffffc830643L, 0xfffffffffc840643L, 0xfffffffffc850644L, 0xfffffffffc860644L, 0xfffffffffc870644L, 0xfffffffffc880645L, 0xfffffffffc890645L, 0xfffffffffc8a0646L, 0xfffffffffc8b0646L,
    0xfffffffffc8c0646L, 0xfffffffffc8d0646L, 0xfffffffffc8e0646L, 0xfffffffffc8f0646L, 0xfffffffffc900649L, 0xfffffffffc91064aL, 0xfffffffffc92064aL, 0xfffffffffc93064aL, 0xfffffffffc94064aL, 0xfffffffffc95064aL,
    0xfffffffffc96064aL, 0xfffffffffc970626L, 0xfffffffffc980626L, 0xfffffffffc990626L, 0xfffffffffc9a0626L, 0xfffffffffc9b0626L, 0xfffffffffc9c0628L, 0xfffffffffc9d0628L, 0xfffffffffc9e0628L, 0xfffffffffc9f0628L,
    0xfffffffffca00628L, 0xfffffffffca1062aL, 0xfffffffffca2062aL, 0xfffffffffca3062aL, 0xfffffffffca4062aL, 0xfffffffffca5062aL, 0xfffffffffca6062bL, 0xfffffffffca7062cL, 0xfffffffffca8062cL, 0xfffffffffca9062dL,
    0xfffffffffcaa062dL, 0xfffffffffcab062eL, 0xfffffffffcac062eL, 0xfffffffffcad0633L, 0xfffffffffcae0633L, 0xfffffffffcaf0633L, 0xfffffffffcb00633L, 0xfffffffffcb10635L, 0xfffffffffcb20635L, 0xfffffffffcb30635L,
    0xfffffffffcb40636L, 0xfffffffffcb50636L, 0xfffffffffcb60636L, 0xfffffffffcb70636L, 0xfffffffffcb80637L, 0xfffffffffcb90638L, 0xfffffffffcba0639L, 0xfffffffffcbb0639L, 0xfffffffffcbc063aL, 0xfffffffffcbd063aL,
    0xfffffffffcbe0641L, 0xfffffffffcbf0641L, 0xfffffffffcc00641L, 0xfffffffffcc10641L, 0xfffffffffcc20642L, 0xfffffffffcc30642L, 0xfffffffffcc40643L, 0xfffffffffcc50643L, 0xfffffffffcc60643L, 0xfffffffffcc70643L,
    0xfffffffffcc80643L, 0xfffffffffcc90644L, 0xfffffffffcca0644L, 0xfffffffffccb0644L, 0xfffffffffccc0644L, 0xfffffffffccd0644L, 0xfffffffffcce0645L, 0xfffffffffccf0645L, 0xfffffffffcd00645L, 0xfffffffffcd10645L,
    0xfffffffffcd20646L, 0xfffffffffcd30646L, 0xfffffffffcd40646L, 0xfffffffffcd50646L, 0xfffffffffcd60646L, 0xfffffffffcd70647L, 0xfffffffffcd80647L, 0xfffffffffcd90647L, 0xfffffffffcda064aL, 0xfffffffffcdb064aL,
    0xfffffffffcdc064aL, 0xfffffffffcdd064aL, 0xfffffffffcde064aL, 0xfffffffffcdf0626L, 0xfffffffffce00626L, 0xfffffffffce10628L, 0xfffffffffce20628L, 0xfffffffffce3062aL, 0xfffffffffce4062aL, 0xfffffffffce5062bL,
    0xfffffffffce6062bL, 0xfffffffffce70633L, 0xfffffffffce80633L, 0xfffffffffce90634L, 0xfffffffffcea0634L, 0xfffffffffceb0643L, 0xfffffffffcec0643L, 0xfffffffffced0644L, 0xfffffffffcee0646L, 0xfffffffffcef0646L,
    0xfffffffffcf0064aL, 0xfffffffffcf1064aL, 0xfffffffffcf20020L, 0xfffffffffcf30020L, 0xfffffffffcf40020L, 0xfffffffffcf50637L, 0xfffffffffcf60637L, 0xfffffffffcf70639L, 0xfffffffffcf80639L, 0xfffffffffcf9063aL,
    0xfffffffffcfa063aL, 0xfffffffffcfb0633L, 0xfffffffffcfc0633L, 0xfffffffffcfd0634L, 0xfffffffffcfe0634L, 0xfffffffffcff062dL, 0xfffffffffd00062dL, 0xfffffffffd01062cL, 0xfffffffffd02062cL, 0xfffffffffd03062eL,
    0xfffffffffd04062eL, 0xfffffffffd050635L, 0xfffffffffd060635L, 0xfffffffffd070636L, 0xfffffffffd080636L, 0xfffffffffd090634L, 0xfffffffffd0a0634L, 0xfffffffffd0b0634L, 0xfffffffffd0c0634L, 0xfffffffffd0d0634L,
    0xfffffffffd0e0633L, 0xfffffffffd0f0635L, 0xfffffffffd100636L, 0xfffffffffd110637L, 0xfffffffffd120637L, 0xfffffffffd130639L, 0xfffffffffd140639L, 0xfffffffffd15063aL, 0xfffffffffd16063aL, 0xfffffffffd170633L,
    0xfffffffffd180633L, 0xfffffffffd190634L, 0xfffffffffd1a0634L, 0xfffffffffd1b062dL, 0xfffffffffd1c062dL, 0xfffffffffd1d062cL, 0xfffffffffd1e062cL, 0xfffffffffd1f062eL, 0xfffffffffd20062eL, 0xfffffffffd210635L,
    0xfffffffffd220635L, 0xfffffffffd230636L, 0xfffffffffd240636L, 0xfffffffffd250634L, 0xfffffffffd260634L, 0xfffffffffd270634L, 0xfffffffffd280634L, 0xfffffffffd290634L, 0xfffffffffd2a0633L, 0xfffffffffd2b0635L,
    0xfffffffffd2c0636L, 0xfffffffffd2d0634L, 0xfffffffffd2e0634L, 0xfffffffffd2f0634L, 0xfffffffffd300634L, 0xfffffffffd310633L, 0xfffffffffd320634L, 0xfffffffffd330637L, 0xfffffffffd340633L, 0xfffffffffd350633L,
    0xfffffffffd360633L, 0xfffffffffd370634L, 0xfffffffffd380634L, 0xfffffffffd390634L, 0xfffffffffd3a0637L, 0xfffffffffd3b0638L, 0xfffffffffd3c0627L, 0xfffffffffd3d0627L, 0xfffffffffd50062aL, 0xfffffffffd51062aL,
    0xfffffffffd52062aL, 0xfffffffffd53062aL, 0xfffffffffd54062aL, 0xfffffffffd55062aL, 0xfffffffffd56062aL, 0xfffffffffd57062aL, 0xfffffffffd58062cL, 0xfffffffffd59062cL, 0xfffffffffd5a062dL, 0xfffffffffd5b062dL,
    0xfffffffffd5c0633L, 0xfffffffffd5d0633L, 0xfffffffffd5e0633L, 0xfffffffffd5f0633L, 0xfffffffffd600633L, 0xfffffffffd610633L, 0xfffffffffd620633L, 0xfffffffffd630633L, 0xfffffffffd640635L, 0xfffffffffd650635L,
    0xfffffffffd660635L, 0xfffffffffd670634L, 0xfffffffffd680634L, 0xfffffffffd690634L, 0xfffffffffd6a0634L, 0xfffffffffd6b0634L, 0xfffffffffd6c0634L, 0xfffffffffd6d0634L, 0xfffffffffd6e0636L, 0xfffffffffd6f0636L,
    0xfffffffffd700636L, 0xfffffffffd710637L, 0xfffffffffd720637L, 0xfffffffffd730637L, 0xfffffffffd740637L, 0xfffffffffd750639L, 0xfffffffffd760639L, 0xfffffffffd770639L, 0xfffffffffd780639L, 0xfffffffffd79063aL,
    0xfffffffffd7a063aL, 0xfffffffffd7b063aL, 0xfffffffffd7c0641L, 0xfffffffffd7d0641L, 0xfffffffffd7e0642L, 0xfffffffffd7f0642L, 0xfffffffffd800644L, 0xfffffffffd810644L, 0xfffffffffd820644L, 0xfffffffffd830644L,
    0xfffffffffd840644L, 0xfffffffffd850644L, 0xfffffffffd860644L, 0xfffffffffd870644L, 0xfffffffffd880644L, 0xfffffffffd890645L, 0xfffffffffd8a0645L, 0xfffffffffd8b0645L, 0xfffffffffd8c0645L, 0xfffffffffd8d0645L,
    0xfffffffffd8e0645L, 0xfffffffffd8f0645L, 0xfffffffffd920645L, 0xfffffffffd930647L, 0xfffffffffd940647L, 0xfffffffffd950646L, 0xfffffffffd960646L, 0xfffffffffd970646L, 0xfffffffffd980646L, 0xfffffffffd990646L,
    0xfffffffffd9a0646L, 0xfffffffffd9b0646L, 0xfffffffffd9c064aL, 0xfffffffffd9d064aL, 0xfffffffffd9e0628L, 0xfffffffffd9f062aL, 0xfffffffffda0062aL, 0xfffffffffda1062aL, 0xfffffffffda2062aL, 0xfffffffffda3062aL,
    0xfffffffffda4062aL, 0xfffffffffda5062cL, 0xfffffffffda6062cL, 0xfffffffffda7062cL, 0xfffffffffda80633L, 0xfffffffffda90635L, 0xfffffffffdaa0634L, 0xfffffffffdab0636L, 0xfffffffffdac0644L, 0xfffffffffdad0644L,
    0xfffffffffdae064aL, 0xfffffffffdaf064aL, 0xfffffffffdb0064aL, 0xfffffffffdb10645L, 0xfffffffffdb20642L, 0xfffffffffdb30646L, 0xfffffffffdb40642L, 0xfffffffffdb50644L, 0xfffffffffdb60639L, 0xfffffffffdb70643L,
    0xfffffffffdb80646L, 0xfffffffffdb90645L, 0xfffffffffdba0644L, 0xfffffffffdbb0643L, 0xfffffffffdbc0644L, 0xfffffffffdbd0646L, 0xfffffffffdbe062cL, 0xfffffffffdbf062dL, 0xfffffffffdc00645L, 0xfffffffffdc10641L,
    0xfffffffffdc20628L, 0xfffffffffdc30643L, 0xfffffffffdc40639L, 0xfffffffffdc50635L, 0xfffffffffdc60633L, 0xfffffffffdc70646L, 0xfffffffffdf00635L, 0xfffffffffdf10642L, 0xfffffffffdf20627L, 0xfffffffffdf30627L,
    0xfffffffffdf40645L, 0xfffffffffdf50635L, 0xfffffffffdf60631L, 0xfffffffffdf70639L, 0xfffffffffdf80648L, 0xfffffffffdf90635L, 0xfffffffffe700020L, 0xfffffffffe710640L, 0xfffffffffe720020L, 0xfffffffffe740020L,
    0xfffffffffe760020L, 0xfffffffffe770640L, 0xfffffffffe780020L, 0xfffffffffe790640L, 0xfffffffffe7a0020L, 0xfffffffffe7b0640L, 0xfffffffffe7c0020L, 0xfffffffffe7d0640L, 0xfffffffffe7e0020L, 0xfffffffffe7f0640L,
    0xfffffffffe800621L, 0xfffffffffe810622L, 0xfffffffffe820622L, 0xfffffffffe830623L, 0xfffffffffe840623L, 0xfffffffffe850624L, 0xfffffffffe860624L, 0xfffffffffe870625L, 0xfffffffffe880625L, 0xfffffffffe890626L,
    0xfffffffffe8a0626L, 0xfffffffffe8b0626L, 0xfffffffffe8c0626L, 0xfffffffffe8d0627L, 0xfffffffffe8e0627L, 0xfffffffffe8f0628L, 0xfffffffffe900628L, 0xfffffffffe910628L, 0xfffffffffe920628L, 0xfffffffffe930629L,
    0xfffffffffe940629L, 0xfffffffffe95062aL, 0xfffffffffe96062aL, 0xfffffffffe97062aL, 0xfffffffffe98062aL, 0xfffffffffe99062bL, 0xfffffffffe9a062bL, 0xfffffffffe9b062bL, 0xfffffffffe9c062bL, 0xfffffffffe9d062cL,
    0xfffffffffe9e062cL, 0xfffffffffe9f062cL, 0xfffffffffea0062cL, 0xfffffffffea1062dL, 0xfffffffffea2062dL, 0xfffffffffea3062dL, 0xfffffffffea4062dL, 0xfffffffffea5062eL, 0xfffffffffea6062eL, 0xfffffffffea7062eL,
    0xfffffffffea8062eL, 0xfffffffffea9062fL, 0xfffffffffeaa062fL, 0xfffffffffeab0630L, 0xfffffffffeac0630L, 0xfffffffffead0631L, 0xfffffffffeae0631L, 0xfffffffffeaf0632L, 0xfffffffffeb00632L, 0xfffffffffeb10633L,
    0xfffffffffeb20633L, 0xfffffffffeb30633L, 0xfffffffffeb40633L, 0xfffffffffeb50634L, 0xfffffffffeb60634L, 0xfffffffffeb70634L, 0xfffffffffeb80634L, 0xfffffffffeb90635L, 0xfffffffffeba0635L, 0xfffffffffebb0635L,
    0xfffffffffebc0635L, 0xfffffffffebd0636L, 0xfffffffffebe0636L, 0xfffffffffebf0636L, 0xfffffffffec00636L, 0xfffffffffec10637L, 0xfffffffffec20637L, 0xfffffffffec30637L, 0xfffffffffec40637L, 0xfffffffffec50638L,
    0xfffffffffec60638L, 0xfffffffffec70638L, 0xfffffffffec80638L, 0xfffffffffec90639L, 0xfffffffffeca0639L, 0xfffffffffecb0639L, 0xfffffffffecc0639L, 0xfffffffffecd063aL, 0xfffffffffece063aL, 0xfffffffffecf063aL,
    0xfffffffffed0063aL, 0xfffffffffed10641L, 0xfffffffffed20641L, 0xfffffffffed30641L, 0xfffffffffed40641L, 0xfffffffffed50642L, 0xfffffffffed60642L, 0xfffffffffed70642L, 0xfffffffffed80642L, 0xfffffffffed90643L,
    0xfffffffffeda0643L, 0xfffffffffedb0643L, 0xfffffffffedc0643L, 0xfffffffffedd0644L, 0xfffffffffede0644L, 0xfffffffffedf0644L, 0xfffffffffee00644L, 0xfffffffffee10645L, 0xfffffffffee20645L, 0xfffffffffee30645L,
    0xfffffffffee40645L, 0xfffffffffee50646L, 0xfffffffffee60646L, 0xfffffffffee70646L, 0xfffffffffee80646L, 0xfffffffffee90647L, 0xfffffffffeea0647L, 0xfffffffffeeb0647L, 0xfffffffffeec0647L, 0xfffffffffeed0648L,
    0xfffffffffeee0648L, 0xfffffffffeef0649L, 0xfffffffffef00649L, 0xfffffffffef1064aL, 0xfffffffffef2064aL, 0xfffffffffef3064aL, 0xfffffffffef4064aL, 0xfffffffffef50644L, 0xfffffffffef60644L, 0xfffffffffef70644L,
    0xfffffffffef80644L, 0xfffffffffef90644L, 0xfffffffffefa0644L, 0xfffffffffefb0644L, 0xfffffffffefc0644L, 0xffffffffff210041L, 0xffffffffff220042L, 0xffffffffff230043L, 0xffffffffff240044L, 0xffffffffff250045L,
    0xffffffffff260046L, 0xffffffffff270047L, 0xffffffffff280048L, 0xffffffffff290049L, 0xffffffffff2a004aL, 0xffffffffff2b004bL, 0xffffffffff2c004cL, 0xffffffffff2d004dL, 0xffffffffff2e004eL, 0xffffffffff2f004fL,
    0xffffffffff300050L, 0xffffffffff310051L, 0xffffffffff320052L, 0xffffffffff330053L, 0xffffffffff340054L, 0xffffffffff350055L, 0xffffffffff360056L, 0xffffffffff370057L, 0xffffffffff380058L, 0xffffffffff390059L,
    0xffffffffff3a005aL, 0xffffffffff410061L, 0xffffffffff420062L, 0xffffffffff430063L, 0xffffffffff440064L, 0xffffffffff450065L, 0xffffffffff460066L, 0xffffffffff470067L, 0xffffffffff480068L, 0xffffffffff490069L,
    0xffffffffff4a006aL, 0xffffffffff4b006bL, 0xffffffffff4c006cL, 0xffffffffff4d006dL, 0xffffffffff4e006eL, 0xffffffffff4f006fL, 0xffffffffff500070L, 0xffffffffff510071L, 0xffffffffff520072L, 0xffffffffff530073L,
    0xffffffffff540074L, 0xffffffffff550075L, 0xffffffffff560076L, 0xffffffffff570077L, 0xffffffffff580078L, 0xffffffffff590079L, 0xffffffffff5a007aL, 0xffffffffff6630f2L, 0xffffffffff6730a1L, 0xffffffffff6830a3L,
    0xffffffffff6930a5L, 0xffffffffff6a30a7L, 0xffffffffff6b30a9L, 0xffffffffff6c30e3L, 0xffffffffff6d30e5L, 0xffffffffff6e30e7L, 0xffffffffff6f30c3L, 0xffffffffff7130a2L, 0xffffffffff7230a4L, 0xffffffffff7330a6L,
    0xffffffffff7430a8L, 0xffffffffff7530aaL, 0xffffffffff7630abL, 0xffffffffff7730adL, 0xffffffffff7830afL, 0xffffffffff7930b1L, 0xffffffffff7a30b3L, 0xffffffffff7b30b5L, 0xffffffffff7c30b7L, 0xffffffffff7d30b9L,
    0xffffffffff7e30bbL, 0xffffffffff7f30bdL, 0xffffffffff8030bfL, 0xffffffffff8130c1L, 0xffffffffff8230c4L, 0xffffffffff8330c6L, 0xffffffffff8430c8L, 0xffffffffff8530caL, 0xffffffffff8630cbL, 0xffffffffff8730ccL,
    0xffffffffff8830cdL, 0xffffffffff8930ceL, 0xffffffffff8a30cfL, 0xffffffffff8b30d2L, 0xffffffffff8c30d5L, 0xffffffffff8d30d8L, 0xffffffffff8e30dbL, 0xffffffffff8f30deL, 0xffffffffff9030dfL, 0xffffffffff9130e0L,
    0xffffffffff9230e1L, 0xffffffffff9330e2L, 0xffffffffff9430e4L, 0xffffffffff9530e6L, 0xffffffffff9630e8L, 0xffffffffff9730e9L, 0xffffffffff9830eaL, 0xffffffffff9930ebL, 0xffffffffff9a30ecL, 0xffffffffff9b30edL,
    0xffffffffff9c30efL, 0xffffffffff9d30f3L, 0xffffffffffa03164L, 0xffffffffffa13131L, 0xffffffffffa23132L, 0xffffffffffa33133L, 0xffffffffffa43134L, 0xffffffffffa53135L, 0xffffffffffa63136L, 0xffffffffffa73137L,
    0xffffffffffa83138L, 0xffffffffffa93139L, 0xffffffffffaa313aL, 0xffffffffffab313bL, 0xffffffffffac313cL, 0xffffffffffad313dL, 0xffffffffffae313eL, 0xffffffffffaf313fL, 0xffffffffffb03140L, 0xffffffffffb13141L,
    0xffffffffffb23142L, 0xffffffffffb33143L, 0xffffffffffb43144L, 0xffffffffffb53145L, 0xffffffffffb63146L, 0xffffffffffb73147L, 0xffffffffffb83148L, 0xffffffffffb93149L, 0xffffffffffba314aL, 0xffffffffffbb314bL,
    0xffffffffffbc314cL, 0xffffffffffbd314dL, 0xffffffffffbe314eL, 0xffffffffffc2314fL, 0xffffffffffc33150L, 0xffffffffffc43151L, 0xffffffffffc53152L, 0xffffffffffc63153L, 0xffffffffffc73154L, 0xffffffffffca3155L,
    0xffffffffffcb3156L, 0xffffffffffcc3157L, 0xffffffffffcd3158L, 0xffffffffffce3159L, 0xffffffffffcf315aL, 0xffffffffffd2315bL, 0xffffffffffd3315cL, 0xffffffffffd4315dL, 0xffffffffffd5315eL, 0xffffffffffd6315fL,
    0xffffffffffd73160L, 0xffffffffffda3161L, 0xffffffffffdb3162L, 0xffffffffffdc3163L
  };
  private static final char punctTbl[] = {
    '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*',
    '+', ',', '-', '.', '/', ':', ';', '<', '=', '>',
    '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|',
    '}', '~', '\241', '\242', '\243', '\244', '\245', '\246', '\247', '\251',
    '\253', '\254', '\255', '\256', '\260', '\261', '\266', '\267', '\273', '\277',
    '\327', '\367', '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
    '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F', '\u0310', '\u0311',
    '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317', '\u0318', '\u0319', '\u031A', '\u031B',
    '\u031C', '\u031D', '\u031E', '\u031F', '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325',
    '\u0326', '\u0327', '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F',
    '\u0330', '\u0331', '\u0332', '\u0333', '\u0334', '\u0335', '\u0336', '\u0337', '\u0338', '\u0339',
    '\u033A', '\u033B', '\u033C', '\u033D', '\u033E', '\u033F', '\u0340', '\u0341', '\u0342', '\u0343',
    '\u0344', '\u0345', '\u0360', '\u0361', '\u0375', '\u037E', '\u0387', '\u0482', '\u0483', '\u0484',
    '\u0485', '\u0486', '\u055A', '\u055B', '\u055C', '\u055D', '\u055E', '\u055F', '\u0589', '\u0591',
    '\u0592', '\u0593', '\u0594', '\u0595', '\u0596', '\u0597', '\u0598', '\u0599', '\u059A', '\u059B',
    '\u059C', '\u059D', '\u059E', '\u059F', '\u05A0', '\u05A1', '\u05A3', '\u05A4', '\u05A5', '\u05A6',
    '\u05A7', '\u05A8', '\u05A9', '\u05AA', '\u05AB', '\u05AC', '\u05AD', '\u05AE', '\u05AF', '\u05B0',
    '\u05B1', '\u05B2', '\u05B3', '\u05B4', '\u05B5', '\u05B6', '\u05B7', '\u05B8', '\u05B9', '\u05BB',
    '\u05BC', '\u05BD', '\u05BE', '\u05BF', '\u05C0', '\u05C1', '\u05C2', '\u05C3', '\u05C4', '\u05F3',
    '\u05F4', '\u060C', '\u061B', '\u061F', '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650',
    '\u0651', '\u0652', '\u066A', '\u066B', '\u066C', '\u066D', '\u0670', '\u06D4', '\u06D6', '\u06D7',
    '\u06D8', '\u06D9', '\u06DA', '\u06DB', '\u06DC', '\u06DD', '\u06DE', '\u06DF', '\u06E0', '\u06E1',
    '\u06E2', '\u06E3', '\u06E4', '\u06E7', '\u06E8', '\u06E9', '\u06EA', '\u06EB', '\u06EC', '\u06ED',
    '\u0901', '\u0902', '\u0903', '\u093C', '\u093D', '\u093E', '\u093F', '\u0940', '\u0941', '\u0942',
    '\u0943', '\u0944', '\u0945', '\u0946', '\u0947', '\u0948', '\u0949', '\u094A', '\u094B', '\u094C',
    '\u094D', '\u0950', '\u0951', '\u0952', '\u0953', '\u0954', '\u0962', '\u0963', '\u0964', '\u0965',
    '\u0970', '\u0981', '\u0982', '\u0983', '\u09BC', '\u09BE', '\u09BF', '\u09C0', '\u09C1', '\u09C2',
    '\u09C3', '\u09C4', '\u09C7', '\u09C8', '\u09CB', '\u09CC', '\u09CD', '\u09D7', '\u09E2', '\u09E3',
    '\u09F2', '\u09F3', '\u09FA', '\u0A02', '\u0A3C', '\u0A3E', '\u0A3F', '\u0A40', '\u0A41', '\u0A42',
    '\u0A47', '\u0A48', '\u0A4B', '\u0A4C', '\u0A4D', '\u0A70', '\u0A71', '\u0A72', '\u0A73', '\u0A74',
    '\u0A81', '\u0A82', '\u0A83', '\u0ABC', '\u0ABD', '\u0ABE', '\u0ABF', '\u0AC0', '\u0AC1', '\u0AC2',
    '\u0AC3', '\u0AC4', '\u0AC5', '\u0AC7', '\u0AC8', '\u0AC9', '\u0ACB', '\u0ACC', '\u0ACD', '\u0AD0',
    '\u0B01', '\u0B02', '\u0B03', '\u0B3C', '\u0B3D', '\u0B3E', '\u0B3F', '\u0B40', '\u0B41', '\u0B42',
    '\u0B43', '\u0B47', '\u0B48', '\u0B4B', '\u0B4C', '\u0B4D', '\u0B56', '\u0B57', '\u0B70', '\u0B82',
    '\u0B83', '\u0BBE', '\u0BBF', '\u0BC0', '\u0BC1', '\u0BC2', '\u0BC6', '\u0BC7', '\u0BC8', '\u0BCA',
    '\u0BCB', '\u0BCC', '\u0BCD', '\u0BD7', '\u0C01', '\u0C02', '\u0C03', '\u0C3E', '\u0C3F', '\u0C40',
    '\u0C41', '\u0C42', '\u0C43', '\u0C44', '\u0C46', '\u0C47', '\u0C48', '\u0C4A', '\u0C4B', '\u0C4C',
    '\u0C4D', '\u0C55', '\u0C56', '\u0C82', '\u0C83', '\u0CBE', '\u0CBF', '\u0CC0', '\u0CC1', '\u0CC2',
    '\u0CC3', '\u0CC4', '\u0CC6', '\u0CC7', '\u0CC8', '\u0CCA', '\u0CCB', '\u0CCC', '\u0CCD', '\u0CD5',
    '\u0CD6', '\u0D02', '\u0D03', '\u0D3E', '\u0D3F', '\u0D40', '\u0D41', '\u0D42', '\u0D43', '\u0D46',
    '\u0D47', '\u0D48', '\u0D4A', '\u0D4B', '\u0D4C', '\u0D4D', '\u0D57', '\u0E31', '\u0E34', '\u0E35',
    '\u0E36', '\u0E37', '\u0E38', '\u0E39', '\u0E3A', '\u0E3F', '\u0E47', '\u0E48', '\u0E49', '\u0E4A',
    '\u0E4B', '\u0E4C', '\u0E4D', '\u0E4E', '\u0EAF', '\u0EB1', '\u0EB4', '\u0EB5', '\u0EB6', '\u0EB7',
    '\u0EB8', '\u0EB9', '\u0EBB', '\u0EBC', '\u0EC6', '\u0EC8', '\u0EC9', '\u0ECA', '\u0ECB', '\u0ECC',
    '\u0ECD', '\u0F00', '\u0F01', '\u0F02', '\u0F03', '\u0F04', '\u0F05', '\u0F06', '\u0F07', '\u0F08',
    '\u0F09', '\u0F0A', '\u0F0B', '\u0F0C', '\u0F0D', '\u0F0E', '\u0F0F', '\u0F10', '\u0F11', '\u0F12',
    '\u0F13', '\u0F14', '\u0F15', '\u0F16', '\u0F17', '\u0F1A', '\u0F1B', '\u0F1C', '\u0F1D', '\u0F1E',
    '\u0F1F', '\u0F34', '\u0F35', '\u0F36', '\u0F37', '\u0F38', '\u0F39', '\u0F3A', '\u0F3B', '\u0F3C',
    '\u0F3D', '\u0F3E', '\u0F3F', '\u0F71', '\u0F72', '\u0F73', '\u0F74', '\u0F75', '\u0F76', '\u0F77',
    '\u0F78', '\u0F79', '\u0F7A', '\u0F7B', '\u0F7C', '\u0F7D', '\u0F7E', '\u0F7F', '\u0F80', '\u0F81',
    '\u0F82', '\u0F83', '\u0F84', '\u0F85', '\u0F86', '\u0F87', '\u0F88', '\u0F89', '\u0F8A', '\u0F8B',
    '\u0F90', '\u0F91', '\u0F92', '\u0F93', '\u0F94', '\u0F95', '\u0F97', '\u0F99', '\u0F9A', '\u0F9B',
    '\u0F9C', '\u0F9D', '\u0F9E', '\u0F9F', '\u0FA0', '\u0FA1', '\u0FA2', '\u0FA3', '\u0FA4', '\u0FA5',
    '\u0FA6', '\u0FA7', '\u0FA8', '\u0FA9', '\u0FAA', '\u0FAB', '\u0FAC', '\u0FAD', '\u0FB1', '\u0FB2',
    '\u0FB3', '\u0FB4', '\u0FB5', '\u0FB6', '\u0FB7', '\u0FB9', '\u10FB', '\u2010', '\u2011', '\u2012',
    '\u2013', '\u2014', '\u2015', '\u2016', '\u2017', '\u2018', '\u2019', '\u201A', '\u201B', '\u201C',
    '\u201D', '\u201E', '\u201F', '\u2020', '\u2021', '\u2022', '\u2023', '\u2024', '\u2025', '\u2026',
    '\u2027', '\u2030', '\u2031', '\u2032', '\u2033', '\u2034', '\u2035', '\u2036', '\u2037', '\u2038',
    '\u2039', '\u203A', '\u203B', '\u203C', '\u203D', '\u203E', '\u203F', '\u2040', '\u2041', '\u2042',
    '\u2043', '\u2044', '\u2045', '\u2046', '\u207A', '\u207B', '\u207C', '\u207D', '\u207E', '\u208A',
    '\u208B', '\u208C', '\u208D', '\u208E', '\u20A0', '\u20A1', '\u20A2', '\u20A3', '\u20A4', '\u20A5',
    '\u20A6', '\u20A7', '\u20A9', '\u20AA', '\u20AB', '\u20D0', '\u20D1', '\u20D2', '\u20D3', '\u20D4',
    '\u20D5', '\u20D6', '\u20D7', '\u20D8', '\u20D9', '\u20DA', '\u20DB', '\u20DC', '\u20DD', '\u20DE',
    '\u20DF', '\u20E0', '\u20E1', '\u2100', '\u2101', '\u2103', '\u2104', '\u2105', '\u2106', '\u2108',
    '\u2109', '\u2114', '\u211E', '\u211F', '\u2123', '\u2125', '\u2127', '\u2129', '\u2132', '\u2190',
    '\u2191', '\u2192', '\u2193', '\u2194', '\u2195', '\u2196', '\u2197', '\u2198', '\u2199', '\u219A',
    '\u219B', '\u219C', '\u219D', '\u219E', '\u219F', '\u21A0', '\u21A1', '\u21A2', '\u21A3', '\u21A4',
    '\u21A5', '\u21A6', '\u21A7', '\u21A8', '\u21A9', '\u21AA', '\u21AB', '\u21AC', '\u21AD', '\u21AE',
    '\u21AF', '\u21B0', '\u21B1', '\u21B2', '\u21B3', '\u21B4', '\u21B5', '\u21B6', '\u21B7', '\u21B8',
    '\u21B9', '\u21BA', '\u21BB', '\u21BC', '\u21BD', '\u21BE', '\u21BF', '\u21C0', '\u21C1', '\u21C2',
    '\u21C3', '\u21C4', '\u21C5', '\u21C6', '\u21C7', '\u21C8', '\u21C9', '\u21CA', '\u21CB', '\u21CC',
    '\u21CD', '\u21CE', '\u21CF', '\u21D0', '\u21D1', '\u21D2', '\u21D3', '\u21D4', '\u21D5', '\u21D6',
    '\u21D7', '\u21D8', '\u21D9', '\u21DA', '\u21DB', '\u21DC', '\u21DD', '\u21DE', '\u21DF', '\u21E0',
    '\u21E1', '\u21E2', '\u21E3', '\u21E4', '\u21E5', '\u21E6', '\u21E7', '\u21E8', '\u21E9', '\u21EA',
    '\u2200', '\u2201', '\u2202', '\u2203', '\u2204', '\u2205', '\u2206', '\u2207', '\u2208', '\u2209',
    '\u220A', '\u220B', '\u220C', '\u220D', '\u220E', '\u220F', '\u2210', '\u2211', '\u2212', '\u2213',
    '\u2214', '\u2215', '\u2216', '\u2217', '\u2218', '\u2219', '\u221A', '\u221B', '\u221C', '\u221D',
    '\u221E', '\u221F', '\u2220', '\u2221', '\u2222', '\u2223', '\u2224', '\u2225', '\u2226', '\u2227',
    '\u2228', '\u2229', '\u222A', '\u222B', '\u222C', '\u222D', '\u222E', '\u222F', '\u2230', '\u2231',
    '\u2232', '\u2233', '\u2234', '\u2235', '\u2236', '\u2237', '\u2238', '\u2239', '\u223A', '\u223B',
    '\u223C', '\u223D', '\u223E', '\u223F', '\u2240', '\u2241', '\u2242', '\u2243', '\u2244', '\u2245',
    '\u2246', '\u2247', '\u2248', '\u2249', '\u224A', '\u224B', '\u224C', '\u224D', '\u224E', '\u224F',
    '\u2250', '\u2251', '\u2252', '\u2253', '\u2254', '\u2255', '\u2256', '\u2257', '\u2258', '\u2259',
    '\u225A', '\u225B', '\u225C', '\u225D', '\u225E', '\u225F', '\u2260', '\u2261', '\u2262', '\u2263',
    '\u2264', '\u2265', '\u2266', '\u2267', '\u2268', '\u2269', '\u226A', '\u226B', '\u226C', '\u226D',
    '\u226E', '\u226F', '\u2270', '\u2271', '\u2272', '\u2273', '\u2274', '\u2275', '\u2276', '\u2277',
    '\u2278', '\u2279', '\u227A', '\u227B', '\u227C', '\u227D', '\u227E', '\u227F', '\u2280', '\u2281',
    '\u2282', '\u2283', '\u2284', '\u2285', '\u2286', '\u2287', '\u2288', '\u2289', '\u228A', '\u228B',
    '\u228C', '\u228D', '\u228E', '\u228F', '\u2290', '\u2291', '\u2292', '\u2293', '\u2294', '\u2295',
    '\u2296', '\u2297', '\u2298', '\u2299', '\u229A', '\u229B', '\u229C', '\u229D', '\u229E', '\u229F',
    '\u22A0', '\u22A1', '\u22A2', '\u22A3', '\u22A4', '\u22A5', '\u22A6', '\u22A7', '\u22A8', '\u22A9',
    '\u22AA', '\u22AB', '\u22AC', '\u22AD', '\u22AE', '\u22AF', '\u22B0', '\u22B1', '\u22B2', '\u22B3',
    '\u22B4', '\u22B5', '\u22B6', '\u22B7', '\u22B8', '\u22B9', '\u22BA', '\u22BB', '\u22BC', '\u22BD',
    '\u22BE', '\u22BF', '\u22C0', '\u22C1', '\u22C2', '\u22C3', '\u22C4', '\u22C5', '\u22C6', '\u22C7',
    '\u22C8', '\u22C9', '\u22CA', '\u22CB', '\u22CC', '\u22CD', '\u22CE', '\u22CF', '\u22D0', '\u22D1',
    '\u22D2', '\u22D3', '\u22D4', '\u22D5', '\u22D6', '\u22D7', '\u22D8', '\u22D9', '\u22DA', '\u22DB',
    '\u22DC', '\u22DD', '\u22DE', '\u22DF', '\u22E0', '\u22E1', '\u22E2', '\u22E3', '\u22E4', '\u22E5',
    '\u22E6', '\u22E7', '\u22E8', '\u22E9', '\u22EA', '\u22EB', '\u22EC', '\u22ED', '\u22EE', '\u22EF',
    '\u22F0', '\u22F1', '\u2300', '\u2302', '\u2303', '\u2304', '\u2305', '\u2306', '\u2307', '\u2308',
    '\u2309', '\u230A', '\u230B', '\u230C', '\u230D', '\u230E', '\u230F', '\u2310', '\u2311', '\u2312',
    '\u2313', '\u2314', '\u2315', '\u2316', '\u2317', '\u2318', '\u2319', '\u231A', '\u231B', '\u231C',
    '\u231D', '\u231E', '\u231F', '\u2320', '\u2321', '\u2322', '\u2323', '\u2324', '\u2325', '\u2326',
    '\u2327', '\u2328', '\u2329', '\u232A', '\u232B', '\u232C', '\u232D', '\u232E', '\u232F', '\u2330',
    '\u2331', '\u2332', '\u2333', '\u2334', '\u2335', '\u2336', '\u2337', '\u2338', '\u2339', '\u233A',
    '\u233B', '\u233C', '\u233D', '\u233E', '\u233F', '\u2340', '\u2341', '\u2342', '\u2343', '\u2344',
    '\u2345', '\u2346', '\u2347', '\u2348', '\u2349', '\u234A', '\u234B', '\u234C', '\u234D', '\u234E',
    '\u234F', '\u2350', '\u2351', '\u2352', '\u2353', '\u2354', '\u2355', '\u2356', '\u2357', '\u2358',
    '\u2359', '\u235A', '\u235B', '\u235C', '\u235D', '\u235E', '\u235F', '\u2360', '\u2361', '\u2362',
    '\u2363', '\u2364', '\u2365', '\u2366', '\u2367', '\u2368', '\u2369', '\u236A', '\u236B', '\u236C',
    '\u236D', '\u236E', '\u236F', '\u2370', '\u2371', '\u2372', '\u2373', '\u2374', '\u2375', '\u2376',
    '\u2377', '\u2378', '\u2379', '\u237A', '\u2400', '\u2401', '\u2402', '\u2403', '\u2404', '\u2405',
    '\u2406', '\u2407', '\u2408', '\u2409', '\u240A', '\u240B', '\u240C', '\u240D', '\u240E', '\u240F',
    '\u2410', '\u2411', '\u2412', '\u2413', '\u2414', '\u2415', '\u2416', '\u2417', '\u2418', '\u2419',
    '\u241A', '\u241B', '\u241C', '\u241D', '\u241E', '\u241F', '\u2420', '\u2421', '\u2422', '\u2423',
    '\u2424', '\u2440', '\u2441', '\u2442', '\u2443', '\u2444', '\u2445', '\u2446', '\u2447', '\u2448',
    '\u2449', '\u244A', '\u249C', '\u249D', '\u249E', '\u249F', '\u24A0', '\u24A1', '\u24A2', '\u24A3',
    '\u24A4', '\u24A5', '\u24A6', '\u24A7', '\u24A8', '\u24A9', '\u24AA', '\u24AB', '\u24AC', '\u24AD',
    '\u24AE', '\u24AF', '\u24B0', '\u24B1', '\u24B2', '\u24B3', '\u24B4', '\u24B5', '\u2500', '\u2501',
    '\u2502', '\u2503', '\u2504', '\u2505', '\u2506', '\u2507', '\u2508', '\u2509', '\u250A', '\u250B',
    '\u250C', '\u250D', '\u250E', '\u250F', '\u2510', '\u2511', '\u2512', '\u2513', '\u2514', '\u2515',
    '\u2516', '\u2517', '\u2518', '\u2519', '\u251A', '\u251B', '\u251C', '\u251D', '\u251E', '\u251F',
    '\u2520', '\u2521', '\u2522', '\u2523', '\u2524', '\u2525', '\u2526', '\u2527', '\u2528', '\u2529',
    '\u252A', '\u252B', '\u252C', '\u252D', '\u252E', '\u252F', '\u2530', '\u2531', '\u2532', '\u2533',
    '\u2534', '\u2535', '\u2536', '\u2537', '\u2538', '\u2539', '\u253A', '\u253B', '\u253C', '\u253D',
    '\u253E', '\u253F', '\u2540', '\u2541', '\u2542', '\u2543', '\u2544', '\u2545', '\u2546', '\u2547',
    '\u2548', '\u2549', '\u254A', '\u254B', '\u254C', '\u254D', '\u254E', '\u254F', '\u2550', '\u2551',
    '\u2552', '\u2553', '\u2554', '\u2555', '\u2556', '\u2557', '\u2558', '\u2559', '\u255A', '\u255B',
    '\u255C', '\u255D', '\u255E', '\u255F', '\u2560', '\u2561', '\u2562', '\u2563', '\u2564', '\u2565',
    '\u2566', '\u2567', '\u2568', '\u2569', '\u256A', '\u256B', '\u256C', '\u256D', '\u256E', '\u256F',
    '\u2570', '\u2571', '\u2572', '\u2573', '\u2574', '\u2575', '\u2576', '\u2577', '\u2578', '\u2579',
    '\u257A', '\u257B', '\u257C', '\u257D', '\u257E', '\u257F', '\u2580', '\u2581', '\u2582', '\u2583',
    '\u2584', '\u2585', '\u2586', '\u2587', '\u2588', '\u2589', '\u258A', '\u258B', '\u258C', '\u258D',
    '\u258E', '\u258F', '\u2590', '\u2591', '\u2592', '\u2593', '\u2594', '\u2595', '\u25A0', '\u25A1',
    '\u25A2', '\u25A3', '\u25A4', '\u25A5', '\u25A6', '\u25A7', '\u25A8', '\u25A9', '\u25AA', '\u25AB',
    '\u25AC', '\u25AD', '\u25AE', '\u25AF', '\u25B0', '\u25B1', '\u25B2', '\u25B3', '\u25B4', '\u25B5',
    '\u25B6', '\u25B7', '\u25B8', '\u25B9', '\u25BA', '\u25BB', '\u25BC', '\u25BD', '\u25BE', '\u25BF',
    '\u25C0', '\u25C1', '\u25C2', '\u25C3', '\u25C4', '\u25C5', '\u25C6', '\u25C7', '\u25C8', '\u25C9',
    '\u25CA', '\u25CB', '\u25CC', '\u25CD', '\u25CE', '\u25CF', '\u25D0', '\u25D1', '\u25D2', '\u25D3',
    '\u25D4', '\u25D5', '\u25D6', '\u25D7', '\u25D8', '\u25D9', '\u25DA', '\u25DB', '\u25DC', '\u25DD',
    '\u25DE', '\u25DF', '\u25E0', '\u25E1', '\u25E2', '\u25E3', '\u25E4', '\u25E5', '\u25E6', '\u25E7',
    '\u25E8', '\u25E9', '\u25EA', '\u25EB', '\u25EC', '\u25ED', '\u25EE', '\u25EF', '\u2600', '\u2601',
    '\u2602', '\u2603', '\u2604', '\u2605', '\u2606', '\u2607', '\u2608', '\u2609', '\u260A', '\u260B',
    '\u260C', '\u260D', '\u260E', '\u260F', '\u2610', '\u2611', '\u2612', '\u2613', '\u261A', '\u261B',
    '\u261C', '\u261D', '\u261E', '\u261F', '\u2620', '\u2621', '\u2622', '\u2623', '\u2624', '\u2625',
    '\u2626', '\u2627', '\u2628', '\u2629', '\u262A', '\u262B', '\u262C', '\u262D', '\u262E', '\u262F',
    '\u2630', '\u2631', '\u2632', '\u2633', '\u2634', '\u2635', '\u2636', '\u2637', '\u2638', '\u2639',
    '\u263A', '\u263B', '\u263C', '\u263D', '\u263E', '\u263F', '\u2640', '\u2641', '\u2642', '\u2643',
    '\u2644', '\u2645', '\u2646', '\u2647', '\u2648', '\u2649', '\u264A', '\u264B', '\u264C', '\u264D',
    '\u264E', '\u264F', '\u2650', '\u2651', '\u2652', '\u2653', '\u2654', '\u2655', '\u2656', '\u2657',
    '\u2658', '\u2659', '\u265A', '\u265B', '\u265C', '\u265D', '\u265E', '\u265F', '\u2660', '\u2661',
    '\u2662', '\u2663', '\u2664', '\u2665', '\u2666', '\u2667', '\u2668', '\u2669', '\u266A', '\u266B',
    '\u266C', '\u266D', '\u266E', '\u266F', '\u2701', '\u2702', '\u2703', '\u2704', '\u2706', '\u2707',
    '\u2708', '\u2709', '\u270C', '\u270D', '\u270E', '\u270F', '\u2710', '\u2711', '\u2712', '\u2713',
    '\u2714', '\u2715', '\u2716', '\u2717', '\u2718', '\u2719', '\u271A', '\u271B', '\u271C', '\u271D',
    '\u271E', '\u271F', '\u2720', '\u2721', '\u2722', '\u2723', '\u2724', '\u2725', '\u2726', '\u2727',
    '\u2729', '\u272A', '\u272B', '\u272C', '\u272D', '\u272E', '\u272F', '\u2730', '\u2731', '\u2732',
    '\u2733', '\u2734', '\u2735', '\u2736', '\u2737', '\u2738', '\u2739', '\u273A', '\u273B', '\u273C',
    '\u273D', '\u273E', '\u273F', '\u2740', '\u2741', '\u2742', '\u2743', '\u2744', '\u2745', '\u2746',
    '\u2747', '\u2748', '\u2749', '\u274A', '\u274B', '\u274D', '\u274F', '\u2750', '\u2751', '\u2752',
    '\u2756', '\u2758', '\u2759', '\u275A', '\u275B', '\u275C', '\u275D', '\u275E', '\u2761', '\u2762',
    '\u2763', '\u2764', '\u2765', '\u2766', '\u2767', '\u2794', '\u2798', '\u2799', '\u279A', '\u279B',
    '\u279C', '\u279D', '\u279E', '\u279F', '\u27A0', '\u27A1', '\u27A2', '\u27A3', '\u27A4', '\u27A5',
    '\u27A6', '\u27A7', '\u27A8', '\u27A9', '\u27AA', '\u27AB', '\u27AC', '\u27AD', '\u27AE', '\u27AF',
    '\u27B1', '\u27B2', '\u27B3', '\u27B4', '\u27B5', '\u27B6', '\u27B7', '\u27B8', '\u27B9', '\u27BA',
    '\u27BB', '\u27BC', '\u27BD', '\u27BE', '\u3001', '\u3002', '\u3003', '\u3004', '\u3005', '\u3006',
    '\u3008', '\u3009', '\u300A', '\u300B', '\u300C', '\u300D', '\u300E', '\u300F', '\u3010', '\u3011',
    '\u3012', '\u3013', '\u3014', '\u3015', '\u3016', '\u3017', '\u3018', '\u3019', '\u301A', '\u301B',
    '\u301C', '\u301D', '\u301E', '\u301F', '\u3020', '\u302A', '\u302B', '\u302C', '\u302D', '\u302E',
    '\u302F', '\u3030', '\u3031', '\u3032', '\u3033', '\u3034', '\u3035', '\u3036', '\u3037', '\u303F',
    '\u3099', '\u309A', '\u30FB', '\u3190', '\u3191', '\u3200', '\u3201', '\u3202', '\u3203', '\u3204',
    '\u3205', '\u3206', '\u3207', '\u3208', '\u3209', '\u320A', '\u320B', '\u320C', '\u320D', '\u320E',
    '\u320F', '\u3210', '\u3211', '\u3212', '\u3213', '\u3214', '\u3215', '\u3216', '\u3217', '\u3218',
    '\u3219', '\u321A', '\u321B', '\u321C', '\u3220', '\u3221', '\u3222', '\u3223', '\u3224', '\u3225',
    '\u3226', '\u3227', '\u3228', '\u3229', '\u322A', '\u322B', '\u322C', '\u322D', '\u322E', '\u322F',
    '\u3230', '\u3231', '\u3232', '\u3233', '\u3234', '\u3235', '\u3236', '\u3237', '\u3238', '\u3239',
    '\u323A', '\u323B', '\u323C', '\u323D', '\u323E', '\u323F', '\u3240', '\u3241', '\u3242', '\u3243',
    '\u327F', '\u32C0', '\u32C1', '\u32C2', '\u32C3', '\u32C4', '\u32C5', '\u32C6', '\u32C7', '\u32C8',
    '\u32C9', '\u32CA', '\u32CB', '\u3358', '\u3359', '\u335A', '\u335B', '\u335C', '\u335D', '\u335E',
    '\u335F', '\u3360', '\u3361', '\u3362', '\u3363', '\u3364', '\u3365', '\u3366', '\u3367', '\u3368',
    '\u3369', '\u336A', '\u336B', '\u336C', '\u336D', '\u336E', '\u336F', '\u3370', '\u3395', '\u3396',
    '\u3397', '\u3398', '\u339F', '\u33A0', '\u33A1', '\u33A2', '\u33A3', '\u33A4', '\u33A5', '\u33A6',
    '\u33A7', '\u33A8', '\u33AE', '\u33AF', '\u33C2', '\u33C6', '\u33D8', '\u33E0', '\u33E1', '\u33E2',
    '\u33E3', '\u33E4', '\u33E5', '\u33E6', '\u33E7', '\u33E8', '\u33E9', '\u33EA', '\u33EB', '\u33EC',
    '\u33ED', '\u33EE', '\u33EF', '\u33F0', '\u33F1', '\u33F2', '\u33F3', '\u33F4', '\u33F5', '\u33F6',
    '\u33F7', '\u33F8', '\u33F9', '\u33FA', '\u33FB', '\u33FC', '\u33FD', '\u33FE', '\uFB1E', '\uFB29',
    '\uFD3E', '\uFD3F', '\uFDFA', '\uFDFB', '\uFE20', '\uFE21', '\uFE22', '\uFE23', '\uFE30', '\uFE31',
    '\uFE32', '\uFE33', '\uFE34', '\uFE35', '\uFE36', '\uFE37', '\uFE38', '\uFE39', '\uFE3A', '\uFE3B',
    '\uFE3C', '\uFE3D', '\uFE3E', '\uFE3F', '\uFE40', '\uFE41', '\uFE42', '\uFE43', '\uFE44', '\uFE49',
    '\uFE4A', '\uFE4B', '\uFE4C', '\uFE4D', '\uFE4E', '\uFE4F', '\uFE50', '\uFE51', '\uFE52', '\uFE54',
    '\uFE55', '\uFE56', '\uFE57', '\uFE58', '\uFE59', '\uFE5A', '\uFE5B', '\uFE5C', '\uFE5D', '\uFE5E',
    '\uFE5F', '\uFE60', '\uFE61', '\uFE62', '\uFE63', '\uFE64', '\uFE65', '\uFE66', '\uFE68', '\uFE69',
    '\uFE6A', '\uFE6B', '\uFF01', '\uFF02', '\uFF03', '\uFF04', '\uFF05', '\uFF06', '\uFF07', '\uFF08',
    '\uFF09', '\uFF0A', '\uFF0B', '\uFF0C', '\uFF0D', '\uFF0E', '\uFF0F', '\uFF1A', '\uFF1B', '\uFF1C',
    '\uFF1D', '\uFF1E', '\uFF1F', '\uFF20', '\uFF3B', '\uFF3C', '\uFF3D', '\uFF3F', '\uFF5B', '\uFF5C',
    '\uFF5D', '\uFF5E', '\uFF61', '\uFF62', '\uFF63', '\uFF64', '\uFF65', '\uFFE0', '\uFFE1', '\uFFE2',
    '\uFFE4', '\uFFE5', '\uFFE6', '\uFFE8', '\uFFE9', '\uFFEA', '\uFFEB', '\uFFEC', '\uFFED', '\uFFEE',
    '\uFFFD'
  };
  private static final int vowelTbl[] = {
    0, 0, 0x2208222, 0x2208222, 0, 0, 0x3f7cff7f, 0x3f7cff7f, 0x7fc007f, 0x3ff00,
    0xff000, 0x1cfff00, 0x80810040, 0x9800f, -8192, 0xfc003c1f, 0xf07fff, 0, 0x8100000, 256,
    512
  };
  private static final int BITS_PER_INT = 32;

  public UniCharacter() {
  }

  public static boolean isApostrophe(char c) {
    return c == '\'' || c == '\u2019';
  }

  public static boolean isASCII(char c) {
    return c <= '\177';
  }

  public static boolean isDigit(char c) {
    return c > '\377' ? Character.isDigit(c) : (Latin1CharClass[c] & 2) != 0;
  }

  public static boolean isHyphen(char c) {
    return c == '-';
  }

  public static boolean isLetter(char c) {
    return c > '\377' ? Character.isLetter(c) : (Latin1CharClass[c] & 1) != 0;
  }

  public static boolean isLetterOrDigit(char c) {
    return c > '\377' ? Character.isLetterOrDigit(c) : (Latin1CharClass[c] & 3) != 0;
  }

  public static boolean isLowerCase(char c) {
    return c > '\377' ? Character.isLowerCase(c) : (Latin1CharClass[c] & 0x10) != 0;
  }

  public static boolean isPrintable(char c) {
    return isLetterOrDigit(c) || isPunctuation(c) || isWhitespace(c);
  }

  public static boolean isPunctuation(char c) {
    return c > '\377' ? Search.binary(punctTbl, c) >= 0 : (Latin1CharClass[c] & 4) != 0;
  }

  public static boolean isUpperCase(char c) {
    return c > '\377' ? Character.isUpperCase(c) : (Latin1CharClass[c] & 0x20) != 0;
  }

  public static boolean isWhitespace(char c) {
    return c > '\377' ? Character.isUpperCase(c) : (Latin1CharClass[c] & 8) != 0;
  }

  public static boolean isVowel(char c) {
    if (c <= '\377')
      return (Latin1CharClass[c] & 0x40) != 0;
    if (c > '\u029F') {
      return false;
    } else {
      int i = c / 32;
      int j = c & 0x1f;
      return (vowelTbl[i] & 1 << j) != 0;
    }
  }

  public static char nextAccentFromBase(char c, char c1) {
    int i;
    label0: {
      i = 0;
      int j = 0;
      int k = baseTbl.length - 1;
      int l;
      do {
        if (j > k)
          break label0;
        l = (j + k) / 2;
        char c2 = (char)(int)(baseTbl[l] >> 16);
        int j1 = c1 - c2;
        if (j1 < 0) {
          k = l - 1;
          continue;
        }
        if (j1 <= 0)
          break;
        j = l + 1;
      } while (true);
      i = l + 1;
    }
    for (int i1 = i; i1 < baseTbl.length; i1++)
      if ((char)(int)(baseTbl[i1] & 65535L) == c)
        return (char)(int)(baseTbl[i1] >> 16);

    return '\0';
  }

  public static char toBase(char c) {
    if (c <= '\377') {
      char c1 = Latin1ToBase[c];
      if (c1 != 0)
        return c1;
      else
        return c;
    }
    int i = 0;
    for (int j = baseTbl.length - 1; i <= j;) {
      int k = (i + j) / 2;
      char c2 = (char)(int)(baseTbl[k] >> 16);
      int l = c - c2;
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return (char)(int)(baseTbl[k] & 65535L);
    }

    return Character.toUpperCase(c);
  }

}