import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BookingMenu extends JPanel {
    private final Palette palette;
    private final JTabbedPane tabbedPane;
    private final JLabel headerLabel;
    private RoomPanel roomPanel;
    private TourPanel tourPanel;
    private ShowPanel showPanel;
    private CustomCalendar calendar = new CustomCalendar();
    private JComboBox<String> bookingTypeCombo;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public BookingMenu(Palette palette) {
        this.palette = palette;
        this.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new CustomTabbedPaneUI(palette));
        tabbedPane.setBackground(palette.getBackgroundDark());
        tabbedPane.setForeground(palette.getTextColor());

        headerLabel = new JLabel("Bookings", SwingConstants.CENTER);
        headerLabel.setForeground(palette.getTextColor());
        headerLabel.setOpaque(false);
        adjustHeaderFont();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(headerLabel);

        JPanel addBooking = new JPanel();
        bookingTypeCombo = new JComboBox<>(new String[]{"Room", "Tour", "Show"});
        JLabel bookingTypeLabel = new JLabel("Booking Type:");

        addBooking.add(bookingTypeLabel);
        addBooking.add(bookingTypeCombo);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        roomPanel = new RoomPanel();
        tourPanel = new TourPanel();
        showPanel = new ShowPanel();

        cardPanel.add(roomPanel, "Room");
        cardPanel.add(tourPanel, "Tour");
        cardPanel.add(showPanel, "Show");

        bookingTypeCombo.addActionListener(e -> {
            String selected = (String) bookingTypeCombo.getSelectedItem();
            cardLayout.show(cardPanel, selected);
        });

        addBooking.add(cardPanel);
        tabbedPane.addTab("Add Booking", addBooking);
        leftPanel.add(tabbedPane, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        rightPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                rightPanel.repaint();
            }
        });
        rightPanel.setBackground(palette.getBackground());
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(calendar, BorderLayout.CENTER);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(0);
        splitPane.setOpaque(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        this.add(splitPane, BorderLayout.CENTER);

        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int totalWidth = splitPane.getWidth();
                splitPane.setDividerLocation(totalWidth / 2);
            }
        });

        this.add(splitPane, BorderLayout.CENTER);
    }

    private void adjustHeaderFont() {
        int width = getWidth();
        int fontSize = Math.max(20, width / 20);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, fontSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(palette.getBackgroundDark());
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private static class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        private final Palette palette;

        public CustomTabbedPaneUI(Palette palette) {
            this.palette = palette;
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected ? palette.getGreen() : palette.getBackgroundDark());
            g2.fillRect(x, y, w, h);
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // No border
        }
    }
}
