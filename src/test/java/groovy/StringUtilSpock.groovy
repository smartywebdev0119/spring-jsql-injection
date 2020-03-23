package groovy

import org.apache.commons.text.StringEscapeUtils

import com.jsql.util.StringUtil

import spock.lang.Specification

class StringUtilSpock extends Specification {
    
    def stringUtil

    def 'Check decimalHtmlEncode'() {
        
        expect:
            stringUtil.decimalHtmlEncode('���') == '&#233;&#224;&#231;'
            stringUtil.hexstr('313233616263') == '123abc'
            stringUtil.isUtf8('eca') == false
            stringUtil.isUtf8(null) == false
            stringUtil.isUtf8('���') == true
            stringUtil.base64Encode('���') == 'w6nDp8Og'
            stringUtil.base64Decode('w6nDp8Og') == '���'

            stringUtil.compress(null) == null
            stringUtil.decompress(null) == null
            
            StringUtil.toMd4('���') == 'F2C46E2ABB20203FDD7D73B9A1BDBCFA'
            StringUtil.toAdler32('���') == '90505905'
            StringUtil.toCrc16('���') == '7ed8'
            StringUtil.toCrc32('���') == '962770442'
            StringUtil.toCrc64('���') == '-8749774789217878016'
            StringUtil.toHash('md5', '���') == 'AA676AC3C6F41D942ABF5F1CB0F6BAFC'
            StringUtil.toMySql('���') == '48481696940015FA4B6C5947F110340339290749'
            StringUtil.toHex('���') == 'c3a9c3a0c3a7'
            StringUtil.fromHex('c3a9c3a0c3a7') == '���'
            StringUtil.toHexZip('���') == '1fc28b08000000000000007bc3b9c3a039000ac2b6623903000000'
            StringUtil.fromHexZip('1fc28b08000000000000007bc3b9c3a039000ac2b6623903000000') == '���'
            StringUtil.toBase64Zip('���') == 'H8KLCAAAAAAAAAB7w7nDoDkACsK2YjkDAAAA'
            StringUtil.fromBase64Zip('H8KLCAAAAAAAAAB7w7nDoDkACsK2YjkDAAAA') == '���'
            StringUtil.toUrl('���') == '%C3%A9%C3%A0%C3%A7'
            StringUtil.fromUrl('%C3%A9%C3%A0%C3%A7') == '���'
            StringUtil.fromHtml('&eacute;&agrave;&ccedil;') == '���'
            StringUtil.decimalHtmlEncode('<>&���', false) == '<>&&#233;&#224;&#231;'

            StringUtil.toHtml('���') == '&amp;eacute;&amp;agrave;&amp;ccedil;'
            StringUtil.decimalHtmlEncode('<>&���', true) == '&amp;lt;&amp;gt;&amp;&amp;#233;&amp;#224;&amp;#231;'
    }
    
    def setup() {
        stringUtil = new StringUtil()
    }
}