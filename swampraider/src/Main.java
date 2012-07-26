import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Main {
	public static void main(String[] args) throws IOException {
		new Main();
	}
	
	BufferedImage hintergrundGrafik;
	BufferedImage spielerGrafik;
	BufferedImage gegnerGrafik;

	Person spieler;
	List<Person> gegner = new ArrayList<>();

	boolean finished = false;
	
	final JFrame frame = new JFrame("ein Spiel?!");
	
	public Main() throws IOException {
		spielerGrafik = ImageIO.read(new File("grafik/spieler2.png"));
		hintergrundGrafik = ImageIO.read(new File("grafik/hintergrund.png"));
		gegnerGrafik = ImageIO.read(new File("grafik/gegner2.png"));
		
		spieler = new Person(spielerGrafik, 100, 100);
		gegner.add(new Person(gegnerGrafik, 900, 100));
		gegner.add(new Person(gegnerGrafik, 500, 750));
		gegner.add(new Person(gegnerGrafik, 900, 750));
		gegner.add(new Person(gegnerGrafik,900,768));
	    frame.setBounds(50, 50, 1000, 800);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    frame.add(new SpielGrafik());
//	    frame.add(new EinfacheGrafik());

	    
	    frame.addKeyListener(new KeyAdapter() {
	    	@Override
	    	public void keyPressed(KeyEvent e) {
	    		final int farbe = spieler.getFarbe();
	    	
	    		int geschwindigkeit = 10;
	    		if (farbe == -15175956) // wasser
	    			geschwindigkeit = 1;
	    		if (farbe == -3813598) // sand
	    			geschwindigkeit = 5;
	    		if (farbe == -7154364 && ! finished) { // sumpf
	    			finished=true;
	    			JOptionPane.showMessageDialog(frame, "Du bist im Sumpf versunken", "Du bist tot", JOptionPane.INFORMATION_MESSAGE);
	    			System.exit(0);
	    		}
	    		
	    		if (e.getKeyCode() == KeyEvent.VK_UP) 
	    			spieler.bewegeY (-geschwindigkeit);
	    		if (e.getKeyCode() == KeyEvent.VK_DOWN) 
	    			spieler.bewegeY (geschwindigkeit);
	    		if (e.getKeyCode() == KeyEvent.VK_LEFT) 
	    			spieler.bewegeX (-geschwindigkeit);
	    		if (e.getKeyCode() == KeyEvent.VK_RIGHT) 
	    			spieler.bewegeX (geschwindigkeit);

	    		finishedMove();
	    	}
		});

	    final ScheduledExecutorService gegnerScheduler = Executors.newScheduledThreadPool(2);
	    gegnerScheduler.scheduleAtFixedRate(new Runnable() {
	    	@Override
	    	public void run() {
	    		for (Person curGegner: gegner)
	    			bewegeGegner(curGegner);
	    	}
	    }, 200, 100, TimeUnit.MILLISECONDS);
	    
	    frame.setVisible(true);
	}

	private void finishedMove() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (Person curGegner: new ArrayList<>(gegner)) {
					int abstandX = Math.abs(spieler.positionX - curGegner.positionX);
					int abstandY = Math.abs(spieler.positionY - curGegner.positionY);

					if (abstandX < 31 && abstandY < 31) {
						finished=true;
						JOptionPane.showMessageDialog(frame, "Der Gegner hat Dich gefangen", "Du bist tot", JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					}

					if (curGegner.getFarbe() == -7154364) { // sumpf
						gegner.remove(curGegner);
						
						if (gegner.isEmpty()) {
							finished=true;
							JOptionPane.showMessageDialog(frame, "Dein Gegner ist im Sumpf versunken", "Du hast gewonnen", JOptionPane.INFORMATION_MESSAGE);
							System.exit(0);
						}
					}
				}

				frame.repaint();
			}
		});
	}
	
	private void bewegeGegner(Person gegner) {
		if (finished) 
			return;
		
		int abstandX = spieler.positionX - gegner.positionX;
		int abstandY = spieler.positionY - gegner.positionY;
		
		int absolutAbstandX = Math.abs(abstandX);
		int absolutAbstandY = Math.abs(abstandY);
		
		if (absolutAbstandX > absolutAbstandY) {
			gegner.bewegeX (10 * abstandX / absolutAbstandX);
		}
		else {
			gegner.bewegeY (10 * abstandY / absolutAbstandY);
		}

		finishedMove();
	}

	class Person {
		Image img;
		int positionX;
		int positionY;
		
		Person (Image img, int positionX, int positionY) {
			this.img = img;
			this.positionX = positionX;
			this.positionY = positionY;
		}

		int getFarbe() {
    		return hintergrundGrafik.getRGB(positionX+16, positionY+31);
		}
		
		void bewegeX(int geschwindigkeit) {
			positionX = positionX + geschwindigkeit;
			if (positionX < 0) {
				positionX = 0;
			}
			if (positionX >= hintergrundGrafik.getWidth()-16) {
				positionX = hintergrundGrafik.getWidth() - 17;
			}
		}
		void bewegeY(int geschwindigkeit) {
			positionY = positionY + geschwindigkeit;
			if (positionY < 0) 
				positionY = 0;
			if (positionY >= hintergrundGrafik.getHeight()-31)
				positionY = hintergrundGrafik.getHeight() - 32;
		}
		
		void paint(Graphics g) {
			g.drawImage(img, positionX, positionY, null);
		}
	}
	
	class SpielGrafik extends JPanel {
		@Override
		public void paint(Graphics g) {
			g.drawImage(hintergrundGrafik, 0, 0, null);
			spieler.paint(g);
			
			for (Person curGegner: gegner) {
				curGegner.paint(g);
			}
		}
	}
}

class EinfacheGrafik extends JPanel {
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.drawLine(50,50,60,60);
		g.drawLine(50,50,50,60);
	g.drawLine(100,100,200,100);
	g.drawLine(100,200,200,200);
	
	
	
	
	}
}