package gameClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * This class shows the upper panel bar in the GUI.
 * this panel contains info about the game, login fields, and an image.
 */
public class Panel extends JPanel {

    private final JFrame _frame;
    private final Controller _ctrl;
    private Arena _ar;
    private int _scenario_num;
    private static boolean muteFlag;
    private static JTextField _id_field, _s_n;
    private JButton _submit;
    private static JButton un_mute;
    private JLabel _id_label, _level_label, _time, _level, _score, _name_img;
    private static ImageIcon[] _image_sound;

    /**
     * init the panel.
     *
     * @param frame the {@link JFrame} class which contain this panel
     * @param ctrl  {@link Controller}
     */
    public Panel(JFrame frame, Controller ctrl) {
        super();
        _frame = frame;
        _ctrl = ctrl;
        setBackground(Color.gray);
        setBorder(new BevelBorder(BevelBorder.RAISED));

        sound_button();
        insertBox();
        infoBox();
        nameImg();

        setPreferredSize(new Dimension(_frame.getWidth(), 110));
        setLayout(null);
    }

    /**
     * pain and repaint all the element in the panel.
     * set the bounds of the elements to make the panel resizable.
     *
     * @param g this Graphics
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        updateInsertBox();
        updateInfoBox();
        updateNameImg();
        update_sound_button();
    }

    /**
     * init insert box, with getText field to choose new level,
     * and text field to login with id.
     */
    private void insertBox() {
        _id_label = new JLabel("ID: ");
        _level_label = new JLabel("level: ");
        _id_field = new JTextField();
        _s_n = new JTextField();
        _submit = new JButton("Submit");
        _submit.addActionListener(_ctrl);
        _id_label.setForeground(Color.white);
        _level_label.setForeground(Color.white);
        _id_label.setForeground(Color.white);
        add(_submit);
        add(_id_field);
        add(_s_n);
        add(_level_label);
        add(_id_label);
        updateInsertBox();
    }

    /**
     * update bounds of insert box elements
     */
    private void updateInsertBox() {
        _id_field.setBounds(getWidth() - 150, 20, 120, 22);
        _id_label.setBounds(getWidth() - 185, 20, 50, 22);
        _s_n.setBounds(getWidth() - 150, 45, 120, 22);
        _level_label.setBounds(getWidth() - 185, 45, 50, 22);
        _submit.setBounds(getWidth() - 135, 70, 95, 25);
    }

    /**
     * init info box, with timer to end, level, and current score.
     */
    private void infoBox() {
        _time = new JLabel("Time to end: ");
        _time.setForeground(Color.white);
        add(_time);
        _level = new JLabel("Game level: ");
        _level.setForeground(Color.white);
        add(_level);
        _score = new JLabel("Score: ");
        _score.setForeground(Color.white);
        add(_score);
        updateInfoBox();
    }

    /**
     * update bounds of info box elements,
     * also update the info.
     */
    private void updateInfoBox() {
        if (_ar != null) {
            _time.setText("Time to end: " + (int) _ar.getTime() / 1000);
            _level.setText("Game level: " + _scenario_num);
            _score.setText("Score: " + _ar.getGrade());
        }
        _time.setBounds(20, 20, 150, 30);
        _level.setBounds(20, 40, 150, 30);
        _score.setBounds(20, 60, 150, 30);
    }

    /**
     * init name image in the middle of the panel
     */
    private void nameImg() {
        BufferedImage name = null;
        try {
            name = ImageIO.read(new File("img/name.gif"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        _name_img = new JLabel();
        double size = 0.8;
        _name_img.setIcon(new javax.swing.ImageIcon(name.getScaledInstance((int) (324 * size), (int) (124 * size), WIDTH)));
        add(_name_img);
        updateNameImg();
    }

    /**
     * update bounds of name image.
     */
    private void updateNameImg() {
        _name_img.setBounds(_frame.getWidth() / 2 - 150, 0, 300, 100);
    }

    /**
     * init sound button to mute or un-mute the music
     */
    public void sound_button() {
        _image_sound = new ImageIcon[2];

        _image_sound[0] = (new ImageIcon("img/mute.png"));
        _image_sound[1] = (new ImageIcon("img/unmute.png"));

        Image image = _image_sound[0].getImage(); // transform it
        Image new_img = image.getScaledInstance(27, 27, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        _image_sound[0] = new ImageIcon(new_img);

        image = _image_sound[1].getImage(); // transform it
        new_img = image.getScaledInstance(27, 27, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        _image_sound[1] = new ImageIcon(new_img);

        un_mute = new JButton("mute");
        un_mute.setFont(new Font("david", Font.PLAIN, 1));
        un_mute.addActionListener(_ctrl);
        un_mute.setIcon(_image_sound[0]);
        muteFlag = true;
        add(un_mute);
        update_sound_button();
    }

    /**
     * update bounds of sound button.
     */
    public void update_sound_button() {
        un_mute.setBounds(_frame.getWidth() - 250, 27, 40, 40);
    }

    /**
     * Changes the mute button icon when user press the button.
     *
     * @return state of the button: 0 for starting the music, and 1 for stopping the music.
     */
    public static int changeMuteIcon() {
        if (muteFlag) {
            un_mute.setIcon(_image_sound[1]);
            muteFlag = false;
            return 1; //indicate for stopping the music
        } else {
            un_mute.setIcon(_image_sound[0]);
            muteFlag = true;
            return 0; //indicate for starting the music
        }
    }

    // Getter & Setters:

    public void set_ar(Arena ar) {
        _ar = ar;
    }

    public void set_level(int level) {
        _scenario_num = level;
    }

    public static String getLevel() {
        return _s_n.getText();
    }

    public static String getId() {
        return _id_field.getText();
    }

    public JButton get_submit() {
        return _submit;
    }
}
