import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public class CustomCalendar extends JPanel {
    private JLabel monthLabel;
    private JPanel daysPanel;
    private YearMonth currentMonth;
    private StringSender sender = new StringSender();
    private StringReceiver receiver;

    private String selectedDate;

    private final RoomPanel roomPanel;
    private final ShowPanel showPanel;
    private final TourPanel tourPanel;

    public CustomCalendar(StringReceiver receiver, RoomPanel roomPanel, ShowPanel showPanel, TourPanel tourPanel) {
        this.receiver = receiver;
        this.roomPanel = roomPanel;
        this.showPanel = showPanel;
        this.tourPanel = tourPanel;

        sender.setStringListener(receiver);
        setLayout(new BorderLayout());

        currentMonth = YearMonth.now();

        // Top panel with navigation buttons and month label
        JPanel topPanel = new JPanel(new BorderLayout());

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        updateMonthLabel();

        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));

        topPanel.add(prevButton, BorderLayout.WEST);
        topPanel.add(monthLabel, BorderLayout.CENTER);
        topPanel.add(nextButton, BorderLayout.EAST);

        // Panel for weekdays + days
        daysPanel = new JPanel();
        daysPanel.setLayout(new GridLayout(0, 7)); // 7 columns: Mon to Sun

        add(topPanel, BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);

        updateCalendar();
    }

    private void updateMonthLabel() {
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthLabel.setText(monthName + " " + currentMonth.getYear());
    }

    private void updateCalendar() {
        daysPanel.removeAll();

        DayOfWeek[] orderedDays = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };

        for (DayOfWeek day : orderedDays) {
            String dayName = day.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
            daysPanel.add(dayLabel);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        DayOfWeek firstDay = firstOfMonth.getDayOfWeek();

        int dayOffset = (firstDay.getValue() + 6) % 7;

        for (int i = 0; i < dayOffset; i++) {
            daysPanel.add(new JLabel(""));
        }

        int daysInMonth = currentMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            final int dayValue = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.addActionListener(e -> {
                LocalDate date = currentMonth.atDay(dayValue);
                selectedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                roomPanel.setSelectedDate(selectedDate);
                tourPanel.setSelectedDate(selectedDate);
                showPanel.setSelectedDate(selectedDate);
                System.out.println("Selected date: " + selectedDate);
            });
            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private void changeMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        updateMonthLabel();
        updateCalendar();
    }
}
