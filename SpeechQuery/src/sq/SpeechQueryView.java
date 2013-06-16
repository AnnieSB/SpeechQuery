package sq;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * 
 * @author Anh Vu
 * 
 * Graphical user interface of the speech search engine. 
 */
public class SpeechQueryView extends JFrame implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Width and height of the window
    int x = 700;
    int y = 600;

	private Container content;
	private JButton record;
	private JButton playInput;
	private JButton searchAF;
	private JButton searchRhythm;
	private JButton searchMelody;
	private JButton search;
	private JTextPane results;
	//private JTextPane queries;
	
	private JMenuBar mbar;
	private JMenu mode;
	private JRadioButtonMenuItem standard;
	private JRadioButtonMenuItem test;

	ImageIcon play = new ImageIcon("src/Images/Play_Icon.png");
	ImageIcon pause = new ImageIcon("src/Images/Pause.png");
	
	public SpeechQueryView(ActionListener l){
		
		super("SpeechQuery");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    content = getContentPane();
	    
	    mbar = new JMenuBar();
	    mode = new JMenu("Mode");
	    mbar.add(mode);
	    standard = new JRadioButtonMenuItem("Standard");
	    standard.setSelected(true);
	    standard.setActionCommand("Standard");
	    standard.addActionListener(l);
	    mode.add(standard);
	    test = new JRadioButtonMenuItem("Test");
	    test.setSelected(false);
	    test.setActionCommand("Test");
	    test.addActionListener(l);
	    mode.add(test);
		
	    record = new JButton();
	    record.setVisible(true);
	    record.setPreferredSize(new Dimension(100,100));
	    record.setActionCommand("Record");
	    record.addActionListener(l);
	    record.setIcon(new ImageIcon("src/Images/audio-input-microphone.png"));
	    record.setContentAreaFilled(true);
	    JPanel recordPanel = new JPanel();
	    recordPanel.add(record);

	    playInput = new JButton();
	    playInput.setEnabled(false);
	    playInput.setPreferredSize(new Dimension(100,100));
	    playInput.setIcon(play);
	    playInput.setActionCommand("PlayInput");
	    playInput.addActionListener(l);
	    JPanel stop_playPanel = new JPanel();
	    stop_playPanel.add(playInput);
	    
	    search = new JButton();
	    search.setEnabled(false);
	    search.setPreferredSize(new Dimension(100,100));
	    search.setIcon(new ImageIcon("src/Images/magnifier.png"));
	    search.setActionCommand("Search");
	    search.addActionListener(l);
	    JPanel searchPanel = new JPanel();
	    searchPanel.add(search);
	    
	    searchRhythm = new JButton("SR");
	    searchRhythm.setEnabled(false);
	    searchRhythm.setPreferredSize(new Dimension(50,50));
	    searchRhythm.setActionCommand("SearchRhythm");
	    searchRhythm.setVisible(false);
	    searchRhythm.addActionListener(l);
	    searchPanel.add(searchRhythm);
	    
	    searchMelody = new JButton("SM");
	    searchMelody.setEnabled(false);
	    searchMelody.setPreferredSize(new Dimension(50,50));
	    searchMelody.setActionCommand("SearchMelody");
	    searchMelody.setVisible(false);
	    searchMelody.addActionListener(l);
	    searchPanel.add(searchMelody);
	   
	    
	    //queries = new JTextPane();
	    //queries.setEditable(false);
	    //queries.setPreferredSize(new Dimension(200,200));
	    
	    results = new JTextPane();
	    results.setEditable(false);
	    results.setFont(new Font(Font.DIALOG,3,14));
	    results.setText("Hello World!");
	    results.setPreferredSize(new Dimension(300,200));
//	    	StyledDocument doc = (StyledDocument) results.getDocument();
//
//	        Style style = doc.addStyle("StyleName", null);
//	        StyleConstants.setIcon(style, new ImageIcon("src/Images/magnifier.png"));
//            try {
//				doc.insertString(doc.getLength(), "ignored text", style);
//			} catch (BadLocationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	    JScrollPane paneScrollPane = new JScrollPane(results);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(300, 200));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));
	    
	    content.setBackground(Color.lightGray);
	    content.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 1.0;
	    c.gridx = 0;
	    c.gridy = 0;
	    content.add(recordPanel, c);

	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weighty = 1.0;
	    c.gridx = 1;
	    c.gridy = 0;
	    content.add(stop_playPanel, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 1.0;
	    c.gridwidth = 2;
	    c.gridx = 2;
	    c.gridy = 0;
	    content.add(searchPanel, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.ipady = 40;      //make this component tall
	    c.weightx = 1.0;
	    c.weighty = 1.0;
	    c.gridwidth = 4;
	    c.gridx = 0;
	    c.gridy = 1;
	    content.add(paneScrollPane, c);
	    
	    
	    this.setJMenuBar(mbar);
	    this.setContentPane(content);
		this.setPreferredSize(new Dimension(x, y));
		//frame nicht runtersizen
		//bei vergößern entsprechend anpassen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension windowSize = this.getSize();
			int windowX = Math.max(0, (screenSize.width - windowSize.width ) / 2) - x/2;
			int windowY = Math.max(0, (screenSize.height -windowSize.height) / 2) - y/2; 
			this.setLocation(windowX,windowY); 
        this.pack();
		
	}


	public void addText(String newText) {
		String old = this.results.getText();
		this.results.setText(old + "\n" + newText);
	}


	public void activateSearch(){
		this.search.setEnabled(true);
	}
	
	public void deactivateSearch(){
		this.search.setEnabled(false);
	}
	
	public void activatePlayInputButton(){
		this.playInput.setEnabled(true);
	}
	
	public void deactivatePlayInputButton(){
		this.playInput.setEnabled(false);
	}
	
	public void activateRecord(){
		this.record.setEnabled(true);
	}
	
	public JButton getSearchAF() {
		return searchAF;
	}


	public JButton getSearchRhythm() {
		return searchRhythm;
	}


	public JButton getSearchMelody() {
		return searchMelody;
	}


	public JButton getSearch() {
		return search;
	}


	public JRadioButtonMenuItem getStandard() {
		return standard;
	}


	public JRadioButtonMenuItem getTest() {
		return test;
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}	
	
	
}