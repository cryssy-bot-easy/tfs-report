package report.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.ibm.icu.text.CurrencyDisplayNames;
import com.ibm.icu.util.ULocale;
import org.apache.commons.lang.WordUtils;
/**
 * User: Marv
 * Date: 11/9/12
 */

public class ReportUtility {

    private static final String[] tensNames = {
            "",
            " Ten",
            " Twenty",
            " Thirty",
            " Forty",
            " Fifty",
            " Sixty",
            " Seventy",
            " Eighty",
            " Ninety"
    };
    

    private static final String[] numNames = {
            "",
            " One",
            " Two",
            " Three",
            " Four",
            " Five",
            " Six",
            " Seven",
            " Eight",
            " Nine",
            " Ten",
            " Eleven",
            " Twelve",
            " Thirteen",
            " Fourteen",
            " Fifteen",
            " Sixteen",
            " Seventeen",
            " Eighteen",
            " Nineteen"
    };

    private static String convertLessThanOneThousand(Integer number) {
        String soFar;

        if (number % 100 < 20){
            soFar = numNames[number % 100];
            number /= 100;
        }
        else {
            soFar = numNames[number % 10];
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + " Hundred" + soFar;
    }


    public static String convert(BigDecimal amount, String currency) {
        String amountStr = amount.toPlainString();
        String[] amountParts = amountStr.split("\\.");

        Long number = new Long(amountParts[0]);

        // 0 to 999 999 999 999
        if (number == 0) { return "Zero"; }

        String snumber = Long.toString(number);

        // pad with "0"
        String mask = "000000000000";
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);

        // XXXnnnnnnnnn
        int billions = Integer.parseInt(snumber.substring(0,3));
        // nnnXXXnnnnnn
        int millions  = Integer.parseInt(snumber.substring(3,6));
        // nnnnnnXXXnnn
        int hundredThousands = Integer.parseInt(snumber.substring(6,9));
        // nnnnnnnnnXXX
        int thousands = Integer.parseInt(snumber.substring(9,12));

        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1 :
                tradBillions = convertLessThanOneThousand(billions)
                        + " Billion ";
                break;
            default :
                tradBillions = convertLessThanOneThousand(billions)
                        + " Billion ";
        }
        String result =  tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1 :
                tradMillions = convertLessThanOneThousand(millions)
                        + " Million ";
                break;
            default :
                tradMillions = convertLessThanOneThousand(millions)
                        + " Million ";
        }
        result =  result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1 :
                tradHundredThousands = "One Thousand ";
                break;
            default :
                tradHundredThousands = convertLessThanOneThousand(hundredThousands)
                        + " Thousand ";
        }
        result =  result + tradHundredThousands;

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result =  result + tradThousand;

        String[] currParts = CurrencyDisplayNames.getInstance(new ULocale("ENGLISH")).getPluralName(currency,"other").split(" ");
        String currencyPlural = null;
		
		if (currParts.length > 1) {
			currencyPlural = WordUtils.capitalizeFully(currParts[0]) + " " + WordUtils.capitalizeFully(currParts[1]);
		} else {
			currencyPlural = WordUtils.capitalizeFully(currParts[0]);
		}

        result = currencyPlural + ":" + result;

        if(Integer.parseInt(amountParts[1]) > 0) {
            result = result + " and " + Integer.parseInt(amountParts[1]) + "/100";
        }

        // remove extra spaces!
        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }
}
