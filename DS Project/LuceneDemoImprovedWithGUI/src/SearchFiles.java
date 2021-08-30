import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import java.awt.Font;
import java.awt.SystemColor;

public class SearchFiles extends JFrame {
	
	private JPanel contentPane;
	private JTextField QueryField;
	public static JTextArea ResultsArea = new JTextArea();
	public static boolean Next = false;
	public static boolean Previous = false;
	public static int numTotalHits = 0;
	public static int start = 0;
    public static int end = 0;
    public static int hitsPerPage = 10;
    public static TopDocs results;
    public static ScoreDoc[] hits;
    public static boolean raw;
    public static IndexSearcher searcher;
    private JTextField ChooseField;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchFiles frame = new SearchFiles();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * Create the frame.
	 */
	public SearchFiles() {
		setFont(new Font("Dialog", Font.PLAIN, 18));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1020, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblEnterQuery = new JLabel("Enter Query:");
		lblEnterQuery.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblEnterQuery.setBounds(12, 14, 97, 27);
		contentPane.add(lblEnterQuery);
		
		QueryField = new JTextField();
		QueryField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		QueryField.setBounds(101, 9, 381, 35);
		contentPane.add(QueryField);
		QueryField.setColumns(10);
		
		ResultsArea = new JTextArea();
		ResultsArea.setForeground(SystemColor.windowText);
		ResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
		ResultsArea.setBounds(12, 69, 978, 472);
		contentPane.add(ResultsArea);
		
		JButton SearchButton = new JButton("Search");
		SearchButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
		SearchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    			    	    
						String index = "index";
			    	    String field = "contents";
			    	    String queries = null;
			    	    int repeat = 1;
			    	    raw = false;
			    	    String queryString = QueryField.getText();
			    	    ResultsArea.setText(null);
			    	    
			    	    try {
							IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
							searcher = new IndexSearcher(reader);
				    	    ArabicAnalyzer analyzer = new ArabicAnalyzer();

				    	    BufferedReader in = null;
				    	    if (queries != null) {
				    	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
				    	    } else {
				    	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
				    	    }
				    	    QueryParser parser = new QueryParser(field, analyzer);
				    	    while (true) {
				    	      if (queries == null && (queryString == null || queryString == "")) {                        // prompt the user
				    	        ResultsArea.setText(ResultsArea.getText() + "Enter query: " + "\n");
				    	      }
				    	      
				    	      String line = queryString != null ? queryString : in.readLine();

				    	      if (line == null || line.length() == -1) {
				    	        break;
				    	      }

				    	      line = line.trim();
				    	      if (line.length() == 0) {
				    	        break;
				    	      }
				    	      
				    	      Query query;
							try {
								query = parser.parse(queryString);
					    	      ResultsArea.setText(ResultsArea.getText() + "Searching for: " + query.toString(field) + "\n");
				    	          
					    	      if (repeat > 0) {                           // repeat & time as benchmark
					    	        Date start = new Date();
					    	        Date end = new Date();
					    	        for (int i = 0; i < repeat; i++) {
					    	          searcher.search(query, 100);
					    	          end = new Date();
						    	      ResultsArea.setText(ResultsArea.getText() + "Time at repeat number: "+i+" "+(end.getTime()-start.getTime())+"ms" + "\n");
					    	        }
						    	    ResultsArea.setText(ResultsArea.getText() + "Average Time of repeating(100): "+((end.getTime()-start.getTime())/repeat)+"ms" + "\n");
					    	      }

					    	      doPagingSearch(in, query, raw, queries == null && queryString == null);
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				    	      if (queryString != null) {
				    	        break;
				    	      }
				    	    }
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			    	    
			}
		});
		SearchButton.setBounds(494, 9, 97, 36);
		contentPane.add(SearchButton);
		
		JButton PreviousButton = new JButton("Previous");
		PreviousButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
		PreviousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Previous = true;
			    if (Previous == true) {
			    	start = Math.max(0, start - hitsPerPage);
			    }
			    ResultsArea.setText("");
				end = Math.min(numTotalHits, start + hitsPerPage);
				for (int i = start; i < end; i++) {
					  if (raw) {//output raw format //Print
					    ResultsArea.setText("doc="+hits[i].doc+" score="+hits[i].score + "\n");
					    continue;
					  }
					  Document doc;
					try {
						doc = searcher.doc(hits[i].doc);
						String path = doc.get("path");
				      if (path != null) {
				      	//Print
				        ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + path + "\n");
				        String title = doc.get("title");
				        if (title != null) {//Print
					        ResultsArea.setText(ResultsArea.getText() + (i+1) + "   Title: " + doc.get("title") + "\n");
				        }
				      } else {//Print
					      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + "No path for this document" + "\n");
				      }
					} catch (IOException e) {// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Print
				ResultsArea.setText(ResultsArea.getText() + "\n" + "Press ");
			    if (start - hitsPerPage >= 0) {
			    	//Print
			    	ResultsArea.setText(ResultsArea.getText() + "Previous Button, ");
			    }
			    if (start + hitsPerPage < numTotalHits) {
			    	//Print
			    	ResultsArea.setText(ResultsArea.getText() + "Next Button, ");
			    }
			    //Print
			    ResultsArea.setText(ResultsArea.getText() + "Quit Button or Choose a Page number." + "\n");
			}
		});
		PreviousButton.setBounds(12, 554, 97, 36);
		contentPane.add(PreviousButton);
		
		JButton NextButton = new JButton("Next");
		NextButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
		NextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Next = true;
				ResultsArea.setText(null);
			    if (Next == true) {
			      if (start + hitsPerPage < numTotalHits) {
			        start+=hitsPerPage;
			      }
			    }
				end = Math.min(numTotalHits, start + hitsPerPage);
			    for (int i = start; i < end; i++) {
			    	if (raw) {                              // output raw format
					  	//Print
					    ResultsArea.setText("doc="+hits[i].doc+" score="+hits[i].score + "\n");
					    continue;
					  }
	
					  Document doc;
					try {
						doc = searcher.doc(hits[i].doc);
						String path = doc.get("path");
					    if (path != null) {
					    	//Print
					      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + path + "\n");
					      String title = doc.get("title");
					      if (title != null) {
					    	//Print
					        ResultsArea.setText(ResultsArea.getText() + (i+1) + "   Title: " + doc.get("title") + "\n");
					      }
					    } else {
					      //Print
					      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + "No path for this document" + "\n");
					    }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Print
				ResultsArea.setText(ResultsArea.getText() + "\n" + "Press ");
			    if (start + hitsPerPage >= 0) {
			    //Print
			    ResultsArea.setText(ResultsArea.getText() + "Previous Button, ");
			    }
			    if (start - hitsPerPage < numTotalHits) {
			    //Print
			    ResultsArea.setText(ResultsArea.getText() + "Next Button, ");
			    }
			    //Print
			    ResultsArea.setText(ResultsArea.getText() + "Quit Button or Choose a page number." + "\n");
			}
		});
		NextButton.setBounds(121, 554, 97, 36);
		contentPane.add(NextButton);
		
		JLabel label = new JLabel("<Results>");
		label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		label.setBounds(486, 51, 74, 16);
		contentPane.add(label);
		
		JButton QuitButton = new JButton("Quit");
		QuitButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
		QuitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(ABORT);
			}
		});
		
		QuitButton.setBounds(893, 554, 97, 36);
		contentPane.add(QuitButton);
		
		JButton ChooseButton = new JButton("Choose");
		ChooseButton.setFont(new Font("Tahoma", Font.PLAIN, 15));
		ChooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int Chosen = Integer.parseInt(ChooseField.getText());
				if ( Chosen > -1 && Chosen < hits.length )
				{
					start = (Chosen*10)-10;
					end = Chosen*10;
					ResultsArea.setText("");
					for (int i = start; i < end; i++) {
				    	if (raw) {                              // output raw format
						  	//Print
						    ResultsArea.setText("doc="+hits[i].doc+" score="+hits[i].score + "\n");
						    continue;
						  }
		
						  Document doc;
						try {
							doc = searcher.doc(hits[i].doc);
							String path = doc.get("path");
						    if (path != null) {
						    	//Print
						      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + path + "\n");
						      String title = doc.get("title");
						      if (title != null) {
						    	//Print
						        ResultsArea.setText(ResultsArea.getText() + (i+1) + "   Title: " + doc.get("title") + "\n");
						      }
						    } else {
						      //Print
						      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + "No path for this document" + "\n");
						    }
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}          
					}
					//Print
					ResultsArea.setText(ResultsArea.getText() + "\n" + "Press ");
				    if (start + hitsPerPage >= 0) {
				    //Print
				    ResultsArea.setText(ResultsArea.getText() + "Previous Button, ");
				    }
				    if (start - hitsPerPage < numTotalHits) {
				    //Print
				    ResultsArea.setText(ResultsArea.getText() + "Next Button, ");
				    }
				    //Print
				    ResultsArea.setText(ResultsArea.getText() + "Quit Button or Choose a page number." + "\n");
				}
				else
				{
					//Print
				    ResultsArea.setText("Page Not Found" + "\n");
				}
			}
		});
		ChooseButton.setBounds(893, 9, 97, 36);
		contentPane.add(ChooseButton);
		
		JLabel lblNewLabel = new JLabel("Choose Page:");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setBounds(634, 14, 102, 27);
		contentPane.add(lblNewLabel);
		
		ChooseField = new JTextField();
		ChooseField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		ChooseField.setBounds(731, 14, 150, 27);
		contentPane.add(ChooseField);
		ChooseField.setColumns(10);
	}
	

	  /**
	   * This demonstrates a typical paging search scenario, where the search engine presents 
	   * pages of size n to the user. The user can then go to the next page if interested in
	   * the next hits.
	   * 
	   * When the query is executed for the first time, then only enough results are collected
	   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	   * is executed another time and all hits are collected.
	   * 
	   */
	  public static void doPagingSearch(BufferedReader in, Query query, boolean raw, boolean interactive) throws IOException {
	    // Collect enough docs to show 5 pages
	    results = searcher.search(query, 10 * hitsPerPage);
	    hits = results.scoreDocs;
	    
	    numTotalHits = Math.toIntExact(results.totalHits);
	    //Printing
	    ResultsArea.setText(ResultsArea.getText() + numTotalHits + " total matching documents" + "\n");
	    
	    start = 0;
	    end = Math.min(numTotalHits, hitsPerPage);
	        
	    while (true) {
	      if (end > hits.length) {
	        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	        System.out.println("Collect more (y/n) ?");
	        String line = in.readLine();
	        if (line.length() == 0 || line.charAt(0) == 'n') {
	          break;
	        }
	        hits = searcher.search(query, numTotalHits).scoreDocs;
	      }
	      end = Math.min(hits.length, start + hitsPerPage);
	      for (int i = start; i < end; i++) {
	        if (raw) {// output raw format //Print
	          ResultsArea.setText(ResultsArea.getText() + "doc="+hits[i].doc+" score="+hits[i].score + "\n");
	          continue;
	        }
	        Document doc = searcher.doc(hits[i].doc);
	        String path = doc.get("path");
	        if (path != null) {//Print
	          ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + path + "\n");
	          String title = doc.get("title");
	          if (title != null) {//Print
		        ResultsArea.setText(ResultsArea.getText() + (i+1) + "   Title: " + doc.get("title") + "\n");
	          }
	        } else {//Print
		      ResultsArea.setText(ResultsArea.getText() + (i+1) + ". " + "No path for this document" + "\n");
	        }
	                  
	      }
	      ResultsArea.setText(ResultsArea.getText() + "\n" + "Press ");
          if (start + hitsPerPage < numTotalHits) {//Print
		    ResultsArea.setText(ResultsArea.getText() + "Next button to go to the page, or Press on Quit button to quit");
          }
	      if (!interactive || end == 0) {
	        break;
	      }
	    }
	  }
}
