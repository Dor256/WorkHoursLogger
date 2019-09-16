package work.app.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import work.app.calendar.Day;
import work.app.calendar.Month;

import static work.app.utils.Utils.millisecondsToHours;
import static work.app.utils.Utils.getDay;
import static work.app.utils.Utils.getMonth;
import static work.app.utils.Utils.getYear;
import static work.app.utils.Utils.getWeekDay;
import static work.app.utils.Utils.getTime;
import static work.app.constants.Constants.CSV_FILE_PATH;
import static work.app.constants.Constants.DATE;
import static work.app.constants.Constants.DAY;
import static work.app.constants.Constants.START;
import static work.app.constants.Constants.FINISH;
import static work.app.constants.Constants.EMAIL_SUBJECT;
import static work.app.constants.Constants.TIME_FORMAT;
import static work.app.constants.Constants.END_OF_MONTH;
import static work.app.constants.HiddenConstants.MY_MAIL;

@Service
public class WorkLoggerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JavaMailSender emailSender;

    public WorkLoggerService() throws SQLException {};

    public void enter(WorkLogger workLogger) throws SQLException {
        String dateString = workLogger.getDateString();
        Month month = getMonth(dateString);
        Day day = getDay(dateString);
        int weekDay = getWeekDay(dateString);
        int year = getYear(dateString);
        jdbcTemplate.update("INSERT INTO LOG(year, month, day, weekday, start) VALUES (?, ?, ?, ?, ?)", 
                        year, month.toString(), day.toString(), weekDay, dateString);
    }

    public void exit(WorkLogger workLogger) throws SQLException, ParseException {
        String dateString = workLogger.getDateString();
        double workHours = Double.parseDouble(String.format("%.2f", calculateWorkHours(workLogger)));
        int weekDay = getWeekDay(dateString);
        Month month = getMonth(dateString);
        jdbcTemplate.update("UPDATE LOG SET finish = ?, hours = ? WHERE weekday = ? AND month = ?", 
                        dateString, workHours, weekDay, month.toString());
    }

    public void generateCSVFile(String dateString) throws IOException, MessagingException {
        int year = getYear(dateString);
        Month month = getMonth(dateString);
        List<WorkEntry> workEntries = queryForWorkEntries(month, year);
        writeToCSVFile(workEntries);
        emailCSV(month.toString());
    }

    private List<WorkEntry> queryForWorkEntries(Month month, int year) {
        Month previousMonth = Month.getPreviousMonth(month);
        return jdbcTemplate.query("SELECT day, start, finish FROM LOG WHERE (month = ? AND weekday <= ?) OR (month = ? AND weekday > ?) AND year = ?",
                new Object[]{ month.toString(), END_OF_MONTH, previousMonth.toString(), END_OF_MONTH,year }, 
                (resultSet, rowNum) -> new WorkEntry(resultSet.getString("day"), resultSet.getString("start"), resultSet.getString("finish")));
    }

    private void writeToCSVFile(List<WorkEntry> workEntries) throws IOException {
        FileWriter csvWriter = new FileWriter(CSV_FILE_PATH);
        csvWriter.write(String.format("%s,%s,%s,%s\n", DATE, DAY, START, FINISH));
        for (WorkEntry entry : workEntries) {
            csvWriter.write(String.format("%s,%s,%s,%s\n", entry.getDateForCSV(), entry.getDay(),
                    entry.getTimeStringForCSV(entry.getStart()), entry.getTimeStringForCSV(entry.getFinish())));
        }
        csvWriter.close();
    }

    private void emailCSV(String monthToLog) throws MessagingException {
        MimeMessage emailMessage = emailSender.createMimeMessage();
        MimeMessageHelper mimeHelper = new MimeMessageHelper(emailMessage, true);
        mimeHelper.setTo(MY_MAIL);
        mimeHelper.setSubject(EMAIL_SUBJECT + monthToLog);
        mimeHelper.setText(EMAIL_SUBJECT + monthToLog);
        File csvFile = new File(CSV_FILE_PATH);
        mimeHelper.addAttachment("Work Hours", csvFile);
        emailSender.send(emailMessage);
    }

    private double calculateWorkHours(WorkLogger workLogger) throws SQLException, ParseException {
        String exitString = workLogger.getDateString();
        int weekDay = getWeekDay(exitString);
        Month month = getMonth(exitString);
        String enterString = jdbcTemplate.queryForObject("SELECT start FROM LOG WHERE weekday = ? AND month = ?", 
                                                            new Object[]{weekDay, month.toString()}, String.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        Date enterDate = dateFormat.parse(getTime(enterString));
        Date exitDate = dateFormat.parse(getTime(exitString));
        return millisecondsToHours(exitDate.getTime() - enterDate.getTime());
    }
}