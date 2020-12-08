package qinyoyo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <h1>simpleimageviewer4j - a simple image viewer</h1>
 * <p>
 * The purpose of this class is to provide a simple way to display images
 * in Java (using Swing) to aid in debugging applications which manipulate
 * images.
 * </p>
 * <p>
 * A simple usage would be to specify images to display like the following:
 * </p>
 * <pre>
// Images that we're working on.
BufferedImage img1 = ...
BufferedImage img2 = ...

// A window to view `img1` and `img2` will be shown.
new Viewer(img1, img2).show();
</pre>
 * <p>
 * Using a Collection (such as a List) to specify images to display is
 * also supported:
 * </p>
 * <pre>
// A `List` containing images that we're working on.
List&lt;BufferedImage&gt; images = ...

// A window to view images contained in `images` will be shown.
new Viewer(images).show();
</pre>
 * 
 * @author coobird
 *
 */
public final class TwoImageViewer {
	private  String logFile;
	private  int index;
	private  List<String> imageFiles1;
	private  List<String> imageFiles2;

	private  int viewWidth,viewHeight;

	public void saveFile() {
		if (imageFiles1.size()>0) {
			try {
				FileWriter writer = new FileWriter(logFile, false);
				for (int i = 0; i < imageFiles1.size(); i++)
					writer.write(imageFiles1.get(i) + " <-> " + imageFiles2.get(i) + "\r\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else new File(logFile).delete();
	}
	public TwoImageViewer(String path, List<String> img1,List<String> img2) {
		logFile=path;
		imageFiles1=img1;
		imageFiles2=img2;
	}
	void showNext(ViewerPanel vp1,ViewerPanel vp2) {
		if (index<imageFiles1.size()-1) {
			index++;
		}
		show(vp1,vp2);
	}

	void showPrev(ViewerPanel vp1,ViewerPanel vp2) {
		if (index>0) {
			index--;
		}
		show(vp1,vp2);
	}
	void show(ViewerPanel vp1,ViewerPanel vp2) {
		File[] images=getImages();
		vp1.updateImage(images[0]);
		vp2.updateImage(images[1]);
	}
	
	private void removeIndex() {
		imageFiles1.remove(index);
		imageFiles2.remove(index);
		if (index>0 && index>=imageFiles1.size()) index--;
	}
	private static final String KEY_HELP = "del/0:save large 1:left      <%s>     2:right 3:both Enter:save";
	void completeIndex(int saveIndex ) {
		if (index>=0 && index<imageFiles1.size()) {
			try {
				File file1 = new File(imageFiles1.get(index));
				File file2 = new File(imageFiles2.get(index));
				if (!file1.exists()) {
					removeIndex();
					return;
				}
				if (!file2.exists()) {
					Files.move(file1.toPath(), file2.toPath());
					removeIndex();
					return;
				}
				if (saveIndex==0) {
					if (file1.length()>file2.length()) saveIndex=1;
					else saveIndex=2;
				}
				switch (saveIndex) {
					case 1:
						file2.delete();
						Files.move(file1.toPath(), file2.toPath());
						removeIndex();
						break;
					case 2:
						file1.delete();
						removeIndex();
						break;
					case 3:
						String n=file1.getAbsolutePath().substring(file1.getAbsolutePath().indexOf(".delete")+8);
						File nf = new File(new File(logFile).getParent()+"\\"+n);
						new File(nf.getParent()).mkdirs();
						Files.move(file1.toPath(),nf.toPath());
						removeIndex();
						break;
					default:	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	File [] getImages()  {
		if (index>=0 && index<imageFiles1.size()) {
			try {
				return new File[]{new File(imageFiles1.get(index)),
						new File(imageFiles2.get(index))};
			} catch (Exception e) {}
		}
		return new File[]{null,null};
	}


	private final class ViewerPanel extends JPanel {
		private File imageFile;
		private BufferedImage image;

		public ViewerPanel(File file) {
			this.imageFile = file;
			try {
				this.image = ImageIO.read(file);
			} catch (IOException e) {
			}
		}

		private void updateImage(File file) {
			this.imageFile = file;
			try {
				this.image = ImageIO.read(file);
			} catch (IOException e) {
			}
			repaint();
			this.getParent().doLayout();
		}
		

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image!=null) {
				float scale = Math.min((float)this.getWidth() / image.getWidth(),(float)this.getHeight()/image.getHeight());
				scale = Math.min(1.0f, scale);
				int iw = (int)(scale*image.getWidth());
				int ih = (int)(scale * image.getHeight());
				int x = (int) ((this.getWidth() - iw) / 2);
				int y = (int) ((this.getHeight()  - ih) / 2);

				g.drawImage(image.getScaledInstance(iw,ih , 0), x, y, null);
				g.drawString(imageFile.getAbsolutePath(),10,10);
				g.drawString("size = " +imageFile.length(),10,30);
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(viewWidth, viewHeight);
		}
	}
	
	private final class NavigationPanel extends JPanel {

		private final JButton prevButton = new JButton("<");
		private final JButton nextButton = new JButton(">");
		private final JLabel indicator;
		private final ViewerPanel vp1,vp2;
		
		public NavigationPanel(final ViewerPanel v1,final ViewerPanel v2) {
			this.vp1 = v1;
			this.vp2 = v2;
			this.setLayout(new GridLayout(1, 0));
			
			indicator = new JLabel();
			indicator.setFont(new Font("Monospaced", Font.PLAIN, 14));
			indicator.setHorizontalAlignment(SwingConstants.CENTER);


			prevButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showPrev(vp1,vp2);
					updateButtonStates();
				}
			});
			prevButton.addKeyListener(new KeyNavigation(this));
			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showNext(vp1,vp2);
					updateButtonStates();
				}
			});
			nextButton.addKeyListener(new KeyNavigation(this));
			this.add(prevButton);
			this.add(indicator);
			this.add(nextButton);
			
			updateButtonStates();
		}
		
		private void updateButtonStates() {
			prevButton.setEnabled(index>0);
			nextButton.setEnabled(index<imageFiles1.size()-1);
			
			if (!prevButton.isEnabled() && prevButton.hasFocus()) {
				nextButton.requestFocus();
			}
			if (!nextButton.isEnabled() && nextButton.hasFocus()) {
				prevButton.requestFocus();
			}
			if (imageFiles1!=null && imageFiles1.size()>0)
				indicator.setText(String.format(KEY_HELP, "" + (index + 1) + " / " + imageFiles1.size()));
			else indicator.setText(String.format(KEY_HELP, "empty"));
		}
	}
	
	private final class KeyNavigation extends KeyAdapter {
		private final NavigationPanel np;

		private KeyNavigation(final NavigationPanel np) {
			this.np = np;
		}
		@Override
		public void keyReleased(KeyEvent e) {
			int key = e.getKeyCode();
			
			if (key == KeyEvent.VK_LEFT) {
				showPrev(np.vp1,np.vp2);
				np.updateButtonStates();
				
			} else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_SPACE) {
				showNext(np.vp1,np.vp2);
				np.updateButtonStates();

			} else if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_0) {
				completeIndex(0);
				show(np.vp1,np.vp2);
				np.updateButtonStates();
			} else if (key == KeyEvent.VK_1) {
				completeIndex(1);
				show(np.vp1,np.vp2);
				np.updateButtonStates();
			} else if (key == KeyEvent.VK_2) {
				completeIndex(2);
				show(np.vp1,np.vp2);
				np.updateButtonStates();
			} else if (key == KeyEvent.VK_3) {
				completeIndex(3);
				show(np.vp1,np.vp2);
				np.updateButtonStates();
			} else if (key == KeyEvent.VK_ENTER) {
				saveFile();
			}
		}
	}
	
	public void show() {

		final JFrame f = new JFrame("Simple Image Viewer");
	
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		File[] images=getImages();
		final ViewerPanel vp1 = new ViewerPanel(images[0]);
		final ViewerPanel vp2 = new ViewerPanel(images[1]);

		// This will allow focus on the Frame after clicking on one of the
		// navigation buttons. This will subsequently allow use of keyboard
		// to change the images.
		vp1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				f.requestFocus();
			}
		});
		vp2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				f.requestFocus();
			}
		});

		final NavigationPanel np = new NavigationPanel(vp1,vp2);
		f.addKeyListener(new KeyNavigation(np));

		DisplayMode displayMode = f.getGraphicsConfiguration().getDevice().getDisplayMode();
		int screenWidth = displayMode.getWidth();
		int screenHeight = displayMode.getHeight();
		f.setBounds(0,0,screenWidth,screenHeight);
	

		viewWidth = screenWidth/2-10;
		viewHeight = screenHeight - 60;

		Container panel0 = new JPanel();
		panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));// 垂直布局
		panel0.add(vp1);
		panel0.add(vp2);
		panel0.setSize(new Dimension(screenWidth,screenHeight-50));


		Container panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));// 垂直布局
		panel.setSize(new Dimension(screenWidth,screenHeight));
		panel.add(panel0);
		panel.add(np);
		
		f.setContentPane(panel);
		
		f.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveFile();
            }

			@Override
			public void windowActivated(WindowEvent e) {
			
			}

			@Override
			public void windowClosed(WindowEvent e) {
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				
			}
		});

		
		
		f.pack();
		f.setVisible(true);
	}

	/**
	 * Displays the graphical image viewer.
	 * <p>
	 * This method will ensure that the viewer is created from the AWT Event
	 * Dispatch Thread (EDT).
	 */
	public void run() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				show();
			}
		});
	}
}
