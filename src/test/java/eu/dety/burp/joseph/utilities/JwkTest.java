/**
 * JOSEPH - JavaScript Object Signing and Encryption Pentesting Helper
 * Copyright (C) 2016 Dennis Detering
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package eu.dety.burp.joseph.utilities;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JwkTest {

    @Test
    public void getRsaPublicKeyWithSingleRsaJwkInputReturnsListWithSingleRsaPublicKeyObject() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException {
        Object jwk = new JSONParser().parse("{\"kty\": \"RSA\", \"use\": \"sig\", \"n\": \"AK9LhraAG8Tz55FnLk99Q1V-rJEAS7PhXcaXK5z4tw0IOWVXVHKf7xXibbPRwQVIyF4YUaoanmrkzUa0aU-oWXGdBsBmo4CIhj8jcY5YZFtZF7ynov_3a-8-dQNcfjc6_1U6bBw95bsP6C-oJhaXmX2fnAuVpcK0BjkQ3zoI7SGikTLGwclPJ1WsvTo2pX3HR6QCc1puvDjaO3gBA0mn_S6q3TL6mOqYDIeD3b6aklNbobHe1QSm1rRLO7I-j7B-qiAGb_gGLTRndBc4ZI-sWkwQGOkZeEugJukgspmWAmFYd821RXQ9M8egqCYsVM7FsEm_raKvSG2ehxFo7ZSVbLM\", \"e\": \"AQAB\"}");

        BigInteger modulus = new BigInteger("22128946737323913239210052479333027707901510060102775675830991813349418659538199300647898430584144500806059458278321518777044762899512296866600872394644380219013320495156971514431190023600729602211122577883306928327035481763181383360484196857466673122026840292263234856687762092039930273840883706411057986999291723263528956058054902470342623926525220419403492184749748080083440782860930153041629788053392850350190345701856884676367792841834106393147716901597512639433053628947682648446566660847625123370647049602729290059736582541200917525808306486312868092094709254446973240693245640735124383753810943940731642145971");
        BigInteger publicExponent = new BigInteger("65537");
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));

        List<PublicKey> publicKeyList = Jwk.getRsaPublicKeys(jwk);

        assertNotNull(publicKeyList);
        assertEquals(publicKeyList.size(), 1);
        assertEquals(publicKey, publicKeyList.get(0));
    }

    @Test
    public void getRsaPublicKeyWithSingleEcJwkInputReturnsEmptyList() throws ParseException {
        Object jwk = new JSONParser().parse("{\"kty\":\"EC\", \"crv\":\"P-256\", \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\", \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\", \"use\":\"enc\", \"kid\":\"1\"}");

        List<PublicKey> publicKeyList = Jwk.getRsaPublicKeys(jwk);

        assertNotNull(publicKeyList);
        assertEquals(new ArrayList<PublicKey>(), publicKeyList);
    }

    @Test
    public void getRsaPublicKeyWithTwoKeysRsaAndEcJwkInputReturnsListWithSingleRsaPublicKeyObject() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException {
        Object jwk = new JSONParser().parse("{\"keys\":[{\"kty\":\"EC\", \"crv\":\"P-256\", \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\", \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\", \"use\":\"enc\", \"kid\":\"1\"},{\"kty\":\"RSA\", \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2 QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\", \"e\":\"AQAB\", \"alg\":\"RS256\", \"kid\":\"2011-04-29\"}]}");
        
        BigInteger modulus = new BigInteger("-5682458471133998388349435224633069349339468533284902336027705964449388702650944577143355769222747143645802928997396746788844516020279137866835783191591333722847996520065852710220990368127004199808401899736680560956431068787853232744544770599290862357208116339307372204787434279492353211428856678472586090071739438019744959023980640454937307928948909285462577350157199294652794553602056459172956409144930859767392952162694193223130644537748426311422152249961861449835863678305650703998634078098161038037616498189346650489296709705191330089861202606165927388552774443778601636463150652207936779357119888256103014675837");
        BigInteger publicExponent = new BigInteger("65537");
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));

        List<PublicKey> publicKeyList = Jwk.getRsaPublicKeys(jwk);

        assertNotNull(publicKeyList);
        assertEquals(publicKeyList.size(), 1);
        assertEquals(publicKey, publicKeyList.get(0));
    }

    @Test
    public void getRsaPublicKeyWithTwoRsaJwkInputReturnsListWithTwoRsaPublicKeyObjects() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException {
        Object jwk = new JSONParser().parse("{\"keys\":[{ \"kty\": \"RSA\",\"use\": \"sig\",\"n\": \"AK9LhraAG8Tz55FnLk99Q1V-rJEAS7PhXcaXK5z4tw0IOWVXVHKf7xXibbPRwQVIyF4YUaoanmrkzUa0aU-oWXGdBsBmo4CIhj8jcY5YZFtZF7ynov_3a-8-dQNcfjc6_1U6bBw95bsP6C-oJhaXmX2fnAuVpcK0BjkQ3zoI7SGikTLGwclPJ1WsvTo2pX3HR6QCc1puvDjaO3gBA0mn_S6q3TL6mOqYDIeD3b6aklNbobHe1QSm1rRLO7I-j7B-qiAGb_gGLTRndBc4ZI-sWkwQGOkZeEugJukgspmWAmFYd821RXQ9M8egqCYsVM7FsEm_raKvSG2ehxFo7ZSVbLM\",\"e\": \"AQAB\" },{\"kty\":\"RSA\",\"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\"e\":\"AQAB\",\"alg\":\"RS256\",\"kid\":\"2011-04-29\"}]}");

        BigInteger modulus = new BigInteger("22128946737323913239210052479333027707901510060102775675830991813349418659538199300647898430584144500806059458278321518777044762899512296866600872394644380219013320495156971514431190023600729602211122577883306928327035481763181383360484196857466673122026840292263234856687762092039930273840883706411057986999291723263528956058054902470342623926525220419403492184749748080083440782860930153041629788053392850350190345701856884676367792841834106393147716901597512639433053628947682648446566660847625123370647049602729290059736582541200917525808306486312868092094709254446973240693245640735124383753810943940731642145971");
        BigInteger publicExponent = new BigInteger("65537");
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));

        BigInteger modulus2 = new BigInteger("-5682458471133998388349435224633069349339468533284902336027705964449388702650944577143355769222747143645802928997396746788844516020279137866835783191591333722847996520065852710220990368127004199808401899736680560956431068787853232744544770599290862357208116339307372204787434279492353211428856678472586090071739438019744959023980640454937307928948909285462577350157199294652794553602056459172956409144930859767392952162694193223130644537748426311422152249961861449835863678305650703998634078098161038037616498189346650489296709705191330089861202606165927388552774443778601636463150652207936779357119888256103014675837");
        BigInteger publicExponent2 = new BigInteger("65537");
        PublicKey publicKey2 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus2, publicExponent2));

        List<PublicKey> publicKeyList = Jwk.getRsaPublicKeys(jwk);

        assertNotNull(publicKeyList);
        assertEquals(publicKeyList.size(), 2);
        assertEquals(publicKey, publicKeyList.get(0));
        assertEquals(publicKey2, publicKeyList.get(1));
    }

}
