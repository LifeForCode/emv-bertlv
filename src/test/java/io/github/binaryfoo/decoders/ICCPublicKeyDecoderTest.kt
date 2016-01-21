package io.github.binaryfoo.decoders

import io.github.binaryfoo.tlv.ISOUtil
import org.junit.Test

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat

public class ICCPublicKeyDecoderTest {

    @Test
    public fun certificateWithRemainder() {
        val recovered = ISOUtil.hex2byte("6A045413330089600044FFFF121400000101019003A028E99BECB507C507243C2E8DF4FE56A0297CD0AE72E2CFA992A98C80788422DBE00A1395B1545B09D66CFAB9ECEAF413E3DFF8227BC80BF6DA7F142B32673C527BB79129B5965C0F5DC4C3732BE6FA284F2469CDC545CD8AF915D2DD4AF2E171F5D36D502C42D0D7519B1CA8D3C689B65CC775687F051B2849BC")
        val byteLengthOfIssuerModulus = 144

        assertThat(decodeICCPublicKeyCertificate(recovered, byteLengthOfIssuerModulus, 0).detail, io.github.binaryfoo.DecodedAsStringMatcher.decodedAsString("""Header: 6A
Format: 04
PAN: 5413330089600044FFFF
Expiry Date (MMYY): 1214
Serial number: 000001
Hash algorithm: 01
Public key algorithm: 01
Public key length: 144
Public key exponent length: 03
Public key: A028E99BECB507C507243C2E8DF4FE56A0297CD0AE72E2CFA992A98C80788422DBE00A1395B1545B09D66CFAB9ECEAF413E3DFF8227BC80BF6DA7F142B32673C527BB79129B5965C0F5DC4C3732BE6FA284F2469CDC545CD8AF915D2DD4AF2E171F5D36D502C
Hash: 42D0D7519B1CA8D3C689B65CC775687F051B2849
Trailer: BC
"""))
    }

}
