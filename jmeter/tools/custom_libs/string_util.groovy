/**
 * Helper class for processing strings.
 *
 * Arguments:
 *   1. varName     String. The var where the util instance save to.
 *
 */

def varName = args[0];

public class StringUtil {

    def generator(String alphabet, int length) {
        new Random().with { (1..length).collect { alphabet[nextInt(alphabet.length())] }.join() }
    }

    def randomNumeric(int length, String format = "%s") {
        sprintf(format, this.generator(('0'..'9').join(), length));
    }

    def randomString(int length, String format = "%s") {
        sprintf(format, this.generator((('a'..'z')+('0'..'9')).join(), length));
    }

    def randomWord(int length, String format = "%s") {
        sprintf(format, this.randomString(new Random().nextInt(length)));
    }

    def randomSentence(int length, String format = "%s") {
        sprintf(format, new Random().with { (1..nextInt(length)).collect { this.randomWord(15) }.join(" ") });
    }

    def randomParagraph(int length, String format = "%s") {
        sprintf(format, new Random().with { (1..nextInt(length)).collect { this.randomSentence(15) + "." }.join(" ") });
    }

}

vars.putObject(varName, new StringUtil());

SampleResult.setSuccessful(true);
SampleResult.setResponseCode("200")
SampleResult.setResponseMessage("");
SampleResult.setResponseData("");
