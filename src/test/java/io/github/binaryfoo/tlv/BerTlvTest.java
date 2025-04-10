package io.github.binaryfoo.tlv;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class BerTlvTest {

  @Test
  public void testNewInstanceWithByteFlagThatIsTooBig() {
    try {
      BerTlv.newInstance(Tag.fromHex("9A"), 256);
      fail();
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void testNewInstanceWithByteFlag255() {
    BerTlvUtils.assertEquals("9A01FF", BerTlv.newInstance(Tag.fromHex("9A"), 255).toBinary());
  }

  @Test
  public void testNewInstanceWithHexString05() {
    BerTlvUtils.assertEquals("9A0105", BerTlv.newInstance(Tag.fromHex("9A"), "05").toBinary());
  }

  @Test
  public void testToBinary_ValueLengthLessThan127() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv tlv = BerTlv.newInstance(tag, new byte[]{0x01, 0x02, 0x03, 0x04});
    BerTlvUtils.assertEquals("9F1A0401020304", tlv.toBinary());
  }

  @Test
  public void testToBinary_ValueLength128() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv tlv = BerTlv.newInstance(tag, new byte[128]);
    BerTlvUtils.assertEquals("9F1A8180" + StringUtils.repeat("00", 128), tlv.toBinary());
  }

  @Test
  public void testToBinary_2Primitives() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), new byte[]{(byte) 1});
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9F1A"), new byte[]{(byte) 3});
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(tlv1, tlv2));

    byte[] tlv1Bytes = tlv1.toBinary();
    byte[] tlv2Bytes = tlv2.toBinary();

    BerTlvUtils.assertEquals("EF" + "07" + ISOUtil.hexString(tlv1Bytes) + ISOUtil.hexString(tlv2Bytes), tlv.toBinary());
  }

  @Test
  public void testToBinary_1PrimitiveAnd1Constructed() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), new byte[]{(byte) 1});
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(BerTlv.newInstance(Tag.fromHex("9F1A"), new byte[]{(byte) 3})));
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("E0"), Arrays.asList(tlv1, tlv2));

    byte[] tlv1Bytes = tlv1.toBinary();
    byte[] tlv2Bytes = tlv2.toBinary();

    BerTlvUtils.assertEquals("E0" + "09" + ISOUtil.hexString(tlv1Bytes) + ISOUtil.hexString(tlv2Bytes), tlv.toBinary());
  }

  @Test
  public void testToHexString_Primitive() {
    assertEquals("E1021234", BerTlv.newInstance(Tag.fromHex("E1"), "1234").toHexString());
  }

  @Test
  public void testGetChildrenPrimitive() {
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("E1"), "1234");
    assertEquals(0, tlv.getChildren().size());
  }

  @Test
  public void testFindTlvConstructed() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9F1A"), 3);
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(tlv1, tlv2));
    assertSame(tlv1, tlv.findTlv(Tag.fromHex("9A")));
    assertSame(tlv2, tlv.findTlv(Tag.fromHex("9F1A")));
    assertNull(tlv.findTlv(Tag.fromHex("00")));
  }

  @Test
  public void testGetChildrenConstructed() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9F1A"), 3);
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(tlv1, tlv2));
    List<BerTlv> children = tlv.getChildren();
    assertEquals(2, children.size());
    assertEquals(tlv1, children.get(0));
    assertEquals(tlv2, children.get(1));
  }

  @Test
  public void testFindTlvsConstructed() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9A"), 3);
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("01"), Arrays.asList(tlv1, tlv2));
    List<BerTlv> matches = tlv.findTlvs(Tag.fromHex("9A"));
    assertEquals(2, matches.size());
    assertSame(tlv1, matches.get(0));
    assertSame(tlv2, matches.get(1));
    assertTrue(tlv.findTlvs(Tag.fromHex("00")).isEmpty());
  }

  @Test
  public void testFindTlvPrimitive() {
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    assertNull(tlv.findTlv(Tag.fromHex("9A")));
  }

  @Test
  public void testParsePrimitive() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv expectedTlv = BerTlv.newInstance(tag, new byte[]{0x01, 0x02, 0x03, 0x04});
    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("9F1A0401020304"));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
  }

  @Test
  public void testParsePrimitive_Length128() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv expectedTlv = BerTlv.newInstance(tag, new byte[128]);
    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("9F1A8180" + StringUtils.repeat("00", 128)));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
  }

  @Test
  public void testParsePrimitive_Length255() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv expectedTlv = BerTlv.newInstance(tag, new byte[255]);
    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("9F1A81FF" + StringUtils.repeat("00", 255)));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
  }

  @Test
  public void testParsePrimitive_Length314() {
    Tag tag = new Tag(new byte[]{(byte) 0x9F, (byte) 0x1A}, true);
    BerTlv expectedTlv = BerTlv.newInstance(tag, new byte[314]);
    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("9F1A82013A" + StringUtils.repeat("00", 314)));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
  }

  @Test
  public void testParseConstructed_2Primitives() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9F1A"), 3);
    BerTlv expectedTlv = BerTlv.newInstance(Tag.fromHex("EF"), tlv1, tlv2);

    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("EF" + "07" + "9A0101" + "9F1A0103"));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());

    BerTlvUtils.assertEquals(tlv1.toBinary(), actualTlv.findTlv(Tag.fromHex("9A")).toBinary());
  }

  @Test
  public void testParseConstructed_1PrimitiveAnd2Constructed() {
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv nestedTag1 = BerTlv.newInstance(Tag.fromHex("9F1A"), 3);
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(nestedTag1));
    BerTlv nestedTag2 = BerTlv.newInstance(Tag.fromHex("8A"), "CC");
    BerTlv tlv3 = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(nestedTag2));
    BerTlv expectedTlv = BerTlv.newInstance(Tag.fromHex("E0"), Arrays.asList(tlv1, tlv2, tlv3));

    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("E0" + "0E" + "9A0101" + "EF049F1A0103" + "EF038A01CC"));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
    List<BerTlv> actualEFTlvs = actualTlv.findTlvs(Tag.fromHex("EF"));
    assertEquals(2, actualEFTlvs.size());
    BerTlv actualEFTlv1 = actualEFTlvs.get(0);
    BerTlv actualEFTlv2 = actualEFTlvs.get(1);
    BerTlvUtils.assertEquals(tlv2.toBinary(), actualEFTlv1.toBinary());
    BerTlvUtils.assertEquals(nestedTag1.toBinary(), actualEFTlv1.findTlv(Tag.fromHex("9F1A")).toBinary());
    BerTlvUtils.assertEquals(tlv3.toBinary(), actualEFTlv2.toBinary());
    BerTlvUtils.assertEquals(nestedTag2.toBinary(), actualEFTlv2.findTlv(Tag.fromHex("8A")).toBinary());
  }

  @Test
  public void testParseConstructed_1ConstructedAnd1Primitive() {
    BerTlv nestedTag = BerTlv.newInstance(Tag.fromHex("9F1A"), 3);
    BerTlv tlv1 = BerTlv.newInstance(Tag.fromHex("EF"), Arrays.asList(nestedTag));
    BerTlv tlv2 = BerTlv.newInstance(Tag.fromHex("9A"), 1);
    BerTlv expectedTlv = BerTlv.newInstance(Tag.fromHex("E0"), Arrays.asList(tlv1, tlv2));

    BerTlv actualTlv = BerTlv.parse(ISOUtil.hex2byte("E0" + "09" + "EF049F1A0103" + "9A0101"));
    BerTlvUtils.assertEquals(expectedTlv.toBinary(), actualTlv.toBinary());
    List<BerTlv> actualEFTlvs = actualTlv.findTlvs(Tag.fromHex("EF"));
    assertEquals(1, actualEFTlvs.size());
    BerTlv actualEFTlv = actualTlv.findTlv(Tag.fromHex("EF"));
    BerTlvUtils.assertEquals(tlv1.toBinary(), actualEFTlv.toBinary());
    BerTlvUtils.assertEquals(nestedTag.toBinary(), actualEFTlv.findTlv(Tag.fromHex("9F1A")).toBinary());

    BerTlv actual9ATlv = actualTlv.findTlv(Tag.fromHex("9A"));
    BerTlvUtils.assertEquals(tlv2.toBinary(), actual9ATlv.toBinary());
  }

  @Test
  public void testParseAsPrimitive() {
    BerTlv tlv = BerTlv.parseAsPrimitiveTag(ISOUtil.hex2byte("E181039F5301"));
    assertEquals(Tag.fromHex("E1"), tlv.getTag());
    assertEquals("9F5301", tlv.getValueAsHexString());
    assertTrue(tlv instanceof PrimitiveBerTlv);
  }

  @Test
  public void testGetValueAsHexString() {
    BerTlv tlv = BerTlv.newInstance(Tag.fromHex("9A"), new byte[]{1, 2, (byte) 0xFF});
    assertEquals("0102FF", tlv.getValueAsHexString());
  }

  @Test
  public void testGetLengthInBytesOfEncodedLength() {
    assertEquals(1, BerTlv.newInstance(Tag.fromHex("9A"), "FF").getLengthInBytesOfEncodedLength());
    assertEquals(2, BerTlv.newInstance(Tag.fromHex("9A"), StringUtils.repeat("A", 400)).getLengthInBytesOfEncodedLength());
    assertEquals(3, BerTlv.newInstance(Tag.fromHex("9A"), StringUtils.repeat("A", 4000)).getLengthInBytesOfEncodedLength());
  }

  @Test
  public void testGetStartIndexOfValue() {
    assertEquals(2, BerTlv.newInstance(Tag.fromHex("9A"), "FF").getStartIndexOfValue());
    assertEquals(3, BerTlv.newInstance(Tag.fromHex("9A"), StringUtils.repeat("A", 400)).getStartIndexOfValue());
    assertEquals(5, BerTlv.newInstance(Tag.fromHex("9F1B"), StringUtils.repeat("A", 4000)).getStartIndexOfValue());
  }

  @Test
  public void testHandlesZeroPadding() {
    String crap = "910A93D60A0F3CC53834303072459F180400004000860E04DA9F5809004B360CA0FF728F689F180400004000860E04DA9F580904B50F23328A5C788500000000000000000000000000000000000000000000";

    List<BerTlv> tlv = BerTlv.parseList(ISOUtil.hex2byte(crap), true);
    assertThat(tlv.get(1).getChildren().size(), is(4));
  }

  @Test
  public void detailInExceptionForInsufficientBytes() {
    try {
      BerTlv.parse(ISOUtil.hex2byte("918211"));
      fail();
    } catch (TlvParseException e) {
      assertThat(e.getMessage(), is("Failed parsing TLV with tag 91: Bad length: expected to read 2 (0x82) bytes. Only have 1."));
    }
  }

  @Test
  public void withNonStandardTag() {
    List<BerTlv> parsed = BerTlv.parseList(ISOUtil.hex2byte("9F84101C9291EA7DB1EA276A8C96999DF512A6"), true, new QuirkListTagMode(Collections.singleton("9F84")));
    assertThat(parsed.get(0).getTag().getHexString(), is("9F84"));
    assertThat(parsed.get(0).getValueAsHexString(), is("1C9291EA7DB1EA276A8C96999DF512A6"));
  }

  @Test
  public void lengthWith0x80EncodedInSingleByte() {
    String example = "709157134761739001010119D22122011143804400000F5F201A5541542041544D2F5465737420436172642030352020202020205F300202019F1F183131343338303434303030303030303030303030303030309F080200969F4401028C159F02069F03069F1A0295055F2A029A039C019F37048D178A029F02069F03069F1A0295055F2A029A039C019F37049F49039F3704";
    List<BerTlv> tlvs = BerTlv.parseList(ISOUtil.hex2byte(example), true);
    assertEquals(1, tlvs.size());
    assertEquals(9, tlvs.get(0).getChildren().size());
  }
}

