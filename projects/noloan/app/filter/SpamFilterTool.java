package projects.noloan.app.filter;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.Logger;
import com.google.startupos.common.MessageDifferencer.Reporter;
import com.google.startupos.common.MessageDifferencer;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import java.io.FileWriter;
import projects.noloan.app.Protos.SmsMessage;
import projects.noloan.app.Protos.SmsMessageList;
import projects.noloan.app.filter.SpamFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.io.IOException;


/** A tool for running the SMS Spam filter. */
public class SpamFilterTool {
  private static final Logger log = Logger.getForClass();

  public static enum CsvColumns {
    SENDER,
    CONTENTS,
  }

  @FlagDesc(name = "messages_prototxt", description = "Prototxt of messages to filter")
  public static final Flag<String> messagesPrototxt = Flag.create("");

  @FlagDesc(name = "messages_csv", description = "CSV of messages to filter")
  public static final Flag<String> messagesCsv = Flag.create("");

  @FlagDesc(name = "output_prototxt", description = "Prototxt of messages detected as spam")
  public static final Flag<String> outputPrototxt = Flag.create("");

  private static void checkFlags() {
    if (messagesPrototxt.get().isEmpty() && messagesCsv.get().isEmpty()) {
      System.out.println("Error: Please set --messages_prototxt or --messages_csv");
      System.exit(1);
    }
    if (!messagesPrototxt.get().isEmpty() && !messagesCsv.get().isEmpty()) {
      System.out.println("Error: Please set only one input file in flags");
      System.exit(1);
    }
    if (outputPrototxt.get().isEmpty()) {
      System.out.println("Error: --output_prototxt must be set");
      System.exit(1);
    }
  }

  private static SmsMessageList getMessagesFromCsv() {
    SmsMessageList.Builder result = SmsMessageList.newBuilder();

    try {
      Reader fileReader = new FileReader(FileUtils.expandHomeDirectory(messagesCsv.get()));
      List<CSVRecord> records =
          CSVFormat.DEFAULT
              .withDelimiter('\t')
              .withSkipHeaderRecord()
              .withHeader(CsvColumns.class)
              .parse(fileReader)
              .getRecords();

      for (CSVRecord record : records) {
        String sender = record.get(CsvColumns.SENDER);
        String contents = record.get(CsvColumns.CONTENTS);
        SmsMessage message =
            SmsMessage.newBuilder()
                .setSender(sender)
                .setContents(contents)
                .build();
        result.addMessage(message);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return result.build();
  }

  /* Returns messages to filter from prototxt or csv */
  private static ImmutableList<SmsMessage> getMessagesToFilter() {
    SmsMessageList messagesToFilter;
    if (!messagesPrototxt.get().isEmpty()) {
      messagesToFilter = (SmsMessageList) FileUtils.readPrototxtUnchecked(
          messagesPrototxt.get(), SmsMessageList.newBuilder());
    } else {
      messagesToFilter = getMessagesFromCsv();
    }
    return ImmutableList.copyOf(messagesToFilter.getMessageList());
  }

  public static void main(String[] args) throws Exception {
    Iterable<String> packages = ImmutableList.of(SpamFilterTool.class.getPackage().getName());
    Flags.parse(args, packages);
    checkFlags();

    ImmutableList<SmsMessage> messagesToFilter = getMessagesToFilter();
    SpamFilter spamFilter = new SpamFilter();
    ImmutableList<SmsMessage> messagesDetectedAsSpam = spamFilter.filter(messagesToFilter);

    FileUtils.writePrototxtUnchecked(
        SmsMessageList.newBuilder().addAllMessage(
            messagesDetectedAsSpam).build(),
            outputPrototxt.get());
  }
}