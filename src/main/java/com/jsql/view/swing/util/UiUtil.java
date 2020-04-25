/*******************************************************************************
 * Copyhacked (H) 2012-2020.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import com.jsql.view.swing.console.SwingAppender;
import com.jsql.view.swing.ui.BorderRoundBlu;
import com.jsql.view.swing.ui.CheckBoxIcon;
import com.jsql.view.swing.ui.CustomBasicComboBoxUI;

/**
 * Build default component appearance, keyboard shortcuts and icons.
 */
@SuppressWarnings("serial")
public final class UiUtil {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    public static final Color COLOR_BLU = new Color(132, 172, 221);
    public static final Color COLOR_GREEN = new Color(0, 128, 0);
    public static final String TEXTPANE_FONT = "TextPane.font";

    public static final Color COLOR_FOCUS_GAINED = (Color) UIManager.get("TabbedPane.selected");
    
    public static final Color COLOR_DEFAULT_BACKGROUND = UIManager.getColor("Panel.background");
    public static final Color COLOR_COMPONENT_BORDER = UIManager.getColor("controlShadow");
    public static final Color COLOR_FOCUS_LOST = new Color(248, 249, 249);
    public static final Border BORDER_FOCUS_LOST = new LineBorder(new Color(218, 218, 218), 1, false);
    public static final Border BORDER_FOCUS_GAINED = new LineBorder(UiUtil.COLOR_BLU, 1, false);
    
    public static final URL URL_GLOBE = UiUtil.class.getClassLoader().getResource("swing/images/icons/globe.png");
    
    public static final Icon ICON_TICK = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/check.png"));
    public static final Icon ICON_SQUARE_RED = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/squareRed.png"));
    public static final Icon ICON_SQUARE_GREY = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/squareGrey.png"));
    public static final Icon ICON_LOADER_GIF = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/spinner.gif"));

    public static final Icon ICON_ADMIN_SERVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/admin.png"));
    public static final Icon ICON_SHELL_SERVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/application_xp_terminal.png"));
    public static final Icon ICON_DATABASE_SERVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/database.png"));
    public static final Icon ICON_FILE_SERVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/file.png"));
    public static final Icon ICON_COG = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/cog.png"));
    public static final Icon ICON_BRUTER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/lock_open.png"));
    public static final Icon ICON_CODER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/textfield.png"));
    public static final Icon ICON_UPLOAD = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/disk.png"));
    public static final Icon ICON_SCANLIST = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/table_multiple.png"));

    public static final Icon ICON_TABLE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/table.png"));
    public static final Icon ICON_TABLE_GO = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/tableGo.png"));
    public static final Icon ICON_DATABASE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/database.png"));
    public static final Icon ICON_DATABASE_GO = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/databaseGo.png"));
    
    public static final Icon ICON_CONSOLE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/console.gif"));
    public static final Icon ICON_HEADER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/header.gif"));
    public static final Icon ICON_CHUNK = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/chunk.gif"));
    public static final Icon ICON_BINARY = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/binary.gif"));
    public static final Icon ICON_CUP = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/cup.png"));
    
    public static final Icon ICON_CLOSE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/close.png"));
    public static final Icon ICON_CLOSE_ROLLOVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/closeRollover.png"));
    public static final Icon ICON_CLOSE_PRESSED = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/closePressed.png"));

    public static final Icon ICON_ARROW_DEFAULT = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/arrowDefault.png"));
    public static final Icon ICON_ARROW_ROLLOVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/arrowRollover.png"));
    public static final Icon ICON_ARROW_PRESSED = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/arrowPressed.png"));

    public static final Icon ICON_FLAG_AR = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/ar.png"));
    public static final Icon ICON_FLAG_ZH = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/zh.png"));
    public static final Icon ICON_FLAG_RU = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/ru.png"));
    public static final Icon ICON_FLAG_TR = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/tr.png"));
    public static final Icon ICON_FLAG_EN = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/en.png"));
    public static final Icon ICON_FLAG_FR = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/fr.png"));
    public static final Icon ICON_FLAG_HI = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/hi.png"));
    public static final Icon ICON_FLAG_CS = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/cs.png"));
    public static final Icon ICON_FLAG_DE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/de.png"));
    public static final Icon ICON_FLAG_NL = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/nl.png"));
    public static final Icon ICON_FLAG_IN_ID = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/id.png"));
    public static final Icon ICON_FLAG_IT = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/it.png"));
    public static final Icon ICON_FLAG_ES = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/es.png"));
    public static final Icon ICON_FLAG_PT = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/pt.png"));
    public static final Icon ICON_FLAG_PL = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/pl.png"));
    public static final Icon ICON_FLAG_JA = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/ja.png"));
    public static final Icon ICON_FLAG_KO = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/ko.png"));
    public static final Icon ICON_FLAG_RO = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/ro.png"));
    public static final Icon ICON_FLAG_LK = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/lk.png"));
    public static final Icon ICON_FLAG_SE = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/flags/se.png"));
    
    public static final URL URL_ICON_16 = UiUtil.class.getClassLoader().getResource("swing/images/software/bug16.png");
    public static final URL URL_ICON_32 = UiUtil.class.getClassLoader().getResource("swing/images/software/bug32.png");
    public static final URL URL_ICON_96 = UiUtil.class.getClassLoader().getResource("swing/images/software/bug96.png");
    public static final URL URL_ICON_128 = UiUtil.class.getClassLoader().getResource("swing/images/software/bug128.png");

    // The drop shadow is created from a PNG image with 8 bit alpha channel.
    public static final Image IMG_SHADOW = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/shadow.png")).getImage();
    
    public static final ImageIcon IMG_BUG = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/bug.png"));
    public static final ImageIcon IMG_STOP_DEFAULT = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/stopDefault.png"));
    public static final ImageIcon IMG_STOP_ROLLOVER = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/stopRollover.png"));
    public static final ImageIcon IMG_STOP_PPRESSED = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/stopPressed.png"));
    
    public static final String PATH_WEB_FOLDERS = "swing/list/payload.txt";
    public static final String INPUT_STREAM_PAGES_SCAN = "swing/list/scan-page.json";

    public static final Icon ICON_EMPTY = new ImageIcon(new BufferedImage(16, 16, Transparency.TRANSLUCENT));

    public static final String PATH_PAUSE = "swing/images/icons/pause.png";
    public static final String PATH_PROGRESSBAR = "swing/images/icons/progressBar.gif";
    
    public static final Icon ICON_ERROR = new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/error.png"));
    
    public static final Border BORDER_BLU = BorderFactory.createCompoundBorder(
        BORDER_FOCUS_GAINED,
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    );
    
    public static final Border BORDER_ROUND_BLU = new BorderRoundBlu();

    public static final String FONT_NAME_UBUNTU_MONO = "Ubuntu Mono";
    public static final String FONT_NAME_UBUNTU_REGULAR = "Ubuntu";
    
    // Used in Translation Dialog
    public static final String FONT_NAME_MONOSPACED = "Monospaced";

    public static final Font FONT_UBUNTU_MONO = new Font(
        UiUtil.FONT_NAME_UBUNTU_MONO,
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextArea.font").getSize() + 2
    );
    
    public static final Font FONT_UBUNTU_REGULAR = new Font(
        UiUtil.FONT_NAME_UBUNTU_REGULAR,
        Font.PLAIN,
        UIManager.getDefaults().getFont(TEXTPANE_FONT).getSize()
    );
    
    public static final Font FONT_SEGOE = new Font(
        "Segoe UI",
        Font.PLAIN,
        UIManager.getDefaults().getFont(TEXTPANE_FONT).getSize()
    );
    
    public static final Font FONT_SEGOE_BIG = new Font(
        UIManager.getDefaults().getFont("TextField.font").getName(),
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextField.font").getSize() + 2
    );

    public static final String CHUNK_VISIBLE = "chunk_visible";
    public static final String BINARY_VISIBLE = "binary_visible";
    public static final String NETWORK_VISIBLE = "header_visible";
    public static final String JAVA_VISIBLE = "java_visible";
    
    private UiUtil() {
        //not called
    }
    
    /**
     * Change the default style of various components.
     */
    public static void prepareGUI() {
        
        loadFonts();
        configureToolTip();
        configureButton();
        configureMenu();
        configureTabbedPane();
        
        UIManager.put("FileChooser.listFont", UiUtil.FONT_SEGOE);
        UIManager.put("FileChooser.listViewBorder", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiUtil.COLOR_BLU),
            BorderFactory.createMatteBorder(2,2,2,2, Color.WHITE)
        ));

        configureText();
        configureTable();
        configureTree();
        configureProgressBar();
        configureComboBox();
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }

    private static void loadFonts() {
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        try (InputStream fontStream = new BufferedInputStream(SwingAppender.class.getClassLoader().getResourceAsStream("swing/font/UbuntuMono-R-ctrlchar.ttf"))) {
            Font ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            ge.registerFont(ubuntuFont);
        } catch (FontFormatException | IOException e) {
            LOGGER.warn("Loading Font Ubuntu Mono with control characters failed", e);
        }
        
        try (InputStream fontStream = new BufferedInputStream(SwingAppender.class.getClassLoader().getResourceAsStream("swing/font/Ubuntu-R.ttf"))) {
            Font ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            ge.registerFont(ubuntuFont);
        } catch (FontFormatException | IOException e) {
            LOGGER.warn("Loading Font Ubuntu failed", e);
        }
    }

    private static void configureText() {
        
        // Custom text component
        // Admin page
        UIManager.put("TextPane.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put(TEXTPANE_FONT, UiUtil.FONT_UBUNTU_MONO);
        UIManager.put("TextPane.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        
        UIManager.put("TextField.border", UiUtil.BORDER_BLU);
        UIManager.put("TextField.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        
        UIManager.put("EditorPane.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        
        UIManager.put("TextArea.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put("TextArea.font", UiUtil.FONT_UBUNTU_MONO);

        // Custom Label
        UIManager.put("Label.font", UiUtil.FONT_SEGOE);
        UIManager.put("Label.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
    }

    private static void configureButton() {
        
        UIManager.put("Button.font", UiUtil.FONT_SEGOE);
        UIManager.put("CheckBox.font", UiUtil.FONT_SEGOE);
        UIManager.put("RadioButton.font", UiUtil.FONT_SEGOE);
        UIManager.put("TitledBorder.font", UiUtil.FONT_SEGOE);

        UIManager.put("Spinner.arrowButtonBorder", UiUtil.BORDER_BLU);
        UIManager.put("Spinner.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiUtil.COLOR_BLU),
            BorderFactory.createMatteBorder(2,2,2,2, Color.WHITE)
        ));
        
        // Custom button
        // Change border of button in default Save as, Confirm dialogs
        UIManager.put("Button.border", UiUtil.BORDER_BLU);
        UIManager.put("Button.select", new Color(155, 193, 232));
        
        // Change border of button in Save as dialog
        UIManager.put("ToggleButton.border", UiUtil.BORDER_BLU);
        
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    }

    private static void configureToolTip() {
        
        // Custom tooltip
        // timer before showing tooltip
        ToolTipManager.sharedInstance().setInitialDelay(750);
        // timer before closing automatically tooltip
        ToolTipManager.sharedInstance().setDismissDelay(30000);
        // timer used when mouse move to another component, show tooltip immediately if timer is not expired
        ToolTipManager.sharedInstance().setReshowDelay(1);

        UIManager.put("ToolTip.background", new Color(255, 255, 225));
        UIManager.put("ToolTip.backgroundInactive", new Color(255, 255, 225));
        UIManager.put("ToolTip.foreground", Color.BLACK);
        UIManager.put("ToolTip.foregroundInactive", Color.BLACK);
        UIManager.put("ToolTip.font", UiUtil.FONT_SEGOE);
    }

    private static void configureTabbedPane() {
        
        // Custom tab
        UIManager.put("TabbedPane.contentAreaColor", UiUtil.FONT_UBUNTU_MONO);
        UIManager.put("TabbedPane.font", UiUtil.FONT_SEGOE);
        // margin of current tab panel
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        // margin above tabs
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 2));
        // margin around tab name
        UIManager.put("TabbedPane.tabInsets", new Insets(2, 3 + 5, 2, 3));
        // lighter unselected tab border
        UIManager.put("TabbedPane.darkShadow", new Color(190,198,205));
    }

    private static void configureMenu() {
        
        // No bold for menu + round corner
        UIManager.put("Menu.font", UiUtil.FONT_SEGOE);
        UIManager.put("Menu.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put("Menu.borderPainted", false);
        UIManager.put("PopupMenu.font", UiUtil.FONT_SEGOE);
        UIManager.put("RadioButtonMenuItem.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put("RadioButtonMenuItem.font", UiUtil.FONT_SEGOE);
        UIManager.put("RadioButtonMenuItem.borderPainted", false);
        UIManager.put("MenuItem.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put("MenuItem.font", UiUtil.FONT_SEGOE);
        UIManager.put("MenuItem.borderPainted", false);
        UIManager.put("MenuItem.disabledAreNavigable", Boolean.TRUE);
        
        UIManager.put("CheckBoxMenuItem.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        UIManager.put("CheckBoxMenuItem.font", UiUtil.FONT_SEGOE);
        UIManager.put("CheckBoxMenuItem.borderPainted", false);
        UIManager.put("CheckBoxMenuItem.checkIcon", new CheckBoxIcon());
    }

    private static void configureTable() {
        
        // Custom table
        UIManager.put("Table.font", UiUtil.FONT_SEGOE);
        UIManager.put("TableHeader.font", UiUtil.FONT_SEGOE);
        UIManager.put("Table.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        
        UIManager.put("Table.focusCellHighlightBorder",
            BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    
                    @Override
                    public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
                        
                        Graphics2D gg = (Graphics2D) g;
                        gg.setColor(Color.GRAY);
                        gg.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0));
                        
                        // Fix #42291: InternalError on drawRect()
                        try {
                            gg.drawRect(x, y, w - 1, h - 1);
                        } catch (InternalError e) {
                            LOGGER.error(e, e);
                        }
                    }
                },
                BorderFactory.createEmptyBorder(0, 1, 0, 0)
            )
        );
    }

    private static void configureTree() {
        
        // Custom tree
        UIManager.put("Tree.expandedIcon", new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/expanded.png")));
        UIManager.put("Tree.collapsedIcon", new ImageIcon(UiUtil.class.getClassLoader().getResource("swing/images/icons/collapsed.png")));
        UIManager.put("Tree.lineTypeDashed", true);
        
        // No default icon for tree nodes
        UIManager.put("Tree.leafIcon", new ImageIcon());
        UIManager.put("Tree.openIcon", new ImageIcon());
        UIManager.put("Tree.closedIcon", new ImageIcon());
    }

    private static void configureComboBox() {
        
        // Custom ComboBox
        UIManager.put("ComboBox.font", UiUtil.FONT_SEGOE);
        UIManager.put("ComboBox.selectionBackground", UiUtil.COLOR_FOCUS_GAINED);
        
        // Use ColorUIResource to preserve the background color for arrow
        UIManager.put("ComboBox.background", new ColorUIResource(Color.WHITE));
        UIManager.put("ComboBox.border", UiUtil.BORDER_BLU);
        UIManager.put("ComboBoxUI", CustomBasicComboBoxUI.class.getName());
    }

    private static void configureProgressBar() {
        
        // Custom progress bar
        UIManager.put("ProgressBar.border", BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(3, 0, 4, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createLineBorder(Color.WHITE)
            )
        ));
        UIManager.put("ProgressBar.foreground", new Color(136, 183, 104));
        UIManager.put("ProgressBar.background", UIManager.get("Tree.background"));
    }

    /**
     * Icons for application window.
     * @return List of a 16x16 (default) and 32x32 icon (alt-tab, taskbar)
     */
    public static List<Image> getIcons() {
        
        List<Image> images = new ArrayList<>();
        
        // Fix #2154: NoClassDefFoundError on read()
        try {
            images.add(ImageIO.read(UiUtil.URL_ICON_128));
            images.add(ImageIO.read(UiUtil.URL_ICON_96));
            images.add(ImageIO.read(UiUtil.URL_ICON_32));
            images.add(ImageIO.read(UiUtil.URL_ICON_16));
        } catch (NoClassDefFoundError | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return images;
    }
}
