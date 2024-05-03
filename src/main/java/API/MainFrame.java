/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package API;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.awt.Window.Type;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import org.codehaus.groovy.util.ListHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.NoSuchElementException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.net.URISyntaxException;
/**
 *
 * @author Admin
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form API_EDIT
     */

    static private ArrayList<News> listNews =  new ArrayList<>(); /// lưu thông tin các bài viết
    static private ArrayList<HashString> lHash = new ArrayList<HashString>(); // Tìm kiếm ký tự cần tìm trong các bài viết
    
    // set up giao diện mặc định
    public MainFrame() {
        initComponents();
        ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Content.setLayout(new BoxLayout(Content, BoxLayout.Y_AXIS));
        Object.setLayout(new BoxLayout(Object, BoxLayout.Y_AXIS));
        Trending.setLayout(new GridLayout(4,1));
        TopTrending();
        updateScrollPane();
        //Khởi tạo toàn bộ bài viết lần đầu tiên
        clearLayout();
        startup();
        updateScrollPane();
        // Thêm panel mới vào Content và cập nhật hiển thị
        Content.revalidate();
        Content.repaint();
     
        // Thêm action cho Enter Key
        Search.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "clickButton");
        Search.getActionMap().put("clickButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Search.doClick();
            }
        });
    }
    
    // khởi tạo thông tin bài viết dưới dạng xâu để tìm kiếm
    private static void setup() {
        FileReader reader = null;
        try {
            /// địa chỉ tương đối này chỉ dùng được với netbeans
            reader = new FileReader("src/main/java/FileStorge/Contents.json");
            
            // tạo một kiểu cho list News
            Gson gson = new Gson();
            java.lang.reflect.Type classOfT = new TypeToken<ArrayList<News>>(){}.getType();

            // Mothod 1: get data in Json with Java
            listNews = gson.fromJson(reader, classOfT);
            
            for(News news : listNews) {
                String res = news.toString();
                HashString cur = new HashString(res.toLowerCase());
                lHash.add(cur);
                lHash.getLast().setHash();
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
    private static void startup(){
        ContentPanel(listNews);
    }
    // Xóa nội dung hiển thị trước đó
    private static void clearLayout() {
        Content.removeAll();
        Content.revalidate();
        Content.repaint();
    }
    private static void clearObject() {
        Object.removeAll();
        Object.revalidate();
        Object.repaint();
    }
    private void TopTrending(){
        String[] top3Hashtag = TrendExport.MostFrequentTag();
     
        JLabel TrendTopic = new JLabel("Trending");
        TrendTopic.setHorizontalAlignment(JLabel.CENTER);
        TrendTopic.setVerticalAlignment(JLabel.CENTER);
        TrendTopic.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Font Arial, kích thước 20
        Font font = TrendTopic.getFont();        
        TrendTopic.setFont(font.deriveFont(font.getStyle() | Font.BOLD ));
        Dimension preferredSize = new Dimension(298, 30);
        TrendTopic.setPreferredSize(preferredSize);        
        Trending.add(TrendTopic);
        
        for (int i =0;i<3;i++){
            String tag = top3Hashtag[i];
            JLabel Tag = new JLabel("       "+tag);
            Tag.setPreferredSize(preferredSize);  
            Tag.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Font Arial, kích thước 20
            
            Tag.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent  e) {
                        clearLayout();
                        clearObject();
                        ArrayList<News> ans;
                        try {
                            ans = searchSuggestion(tag);
                            ContentPanel(ans);
                        } catch (IOException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        updateScrollPane();
                        // Thêm panel mới vào Content và cập nhật hiển thị
                        Content.revalidate();
                        Content.repaint();
                    }
                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        // Khi chuột vào, đặt font in đậm và có gạch chân
                        Font font = Tag.getFont();
                        Tag.setFont(font.deriveFont(font.getStyle() | Font.BOLD ));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        // Khi chuột ra, đặt lại font bình thường
                        Font font = Tag.getFont();
                        Tag.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD ));
                    }
                });
            // Thêm JLabel Tag vào container Trending
            Trending.add(Tag);
        }
    }
    private void EntityFind(String text){
    Entity entity =  EntityFinder.FindEntity(text);
    if(entity!=null){        
        ImageIcon icon = new ImageIcon(entity.getImage());
        JLabel imageOut = new JLabel();
        imageOut.setIcon(icon);
        imageOut.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JLabel nameOut = new JLabel("   "+entity.getFullName());
        JLabel symbolOut = new JLabel("    "+entity.getSymbol());
        Font fontN = new Font("Arial", Font.BOLD, 18);
        Font fontS = new Font("Arial", Font.BOLD, 14);
        nameOut.setFont(fontN);
        symbolOut.setFont(fontS);
        JLabel descripOut = new JLabel();
        descripOut.setText("<html><div style='text-align:justify;'>" + entity.getDescription() + "</div></html>");

        JPanel ImagePanel = new JPanel();
        JPanel TextPanel = new JPanel();                
        imageOut.setHorizontalAlignment(JLabel.CENTER);
        imageOut.setVerticalAlignment(JLabel.CENTER);
        TextPanel.setLayout(new BoxLayout(TextPanel, BoxLayout.Y_AXIS));        
        ImagePanel.add(imageOut);
        TextPanel.add(nameOut);
        if(!entity.getSymbol().equals("Không có Symbol") ) TextPanel.add(symbolOut);
        TextPanel.add(descripOut); 
        descripOut.setPreferredSize(TextPanel.getSize());
        
            {
                clearObject();
                Object.setLayout(new GridLayout(2, 1));
                Object.add(ImagePanel);
                Object.add(TextPanel);
            Object.revalidate();
            Object.repaint();            
            }
    }
    else {
        clearObject();
    }
    }
    // đưa ra xâu chỉ id bài viết chứa ký tự nhập vào
    private ArrayList<News> searchSuggestion(String search) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException {
        ArrayList<News> ans = new ArrayList<News>(); // lưu thông tin vài bài viết chứa ký tự cần tìm
        // search được filter lại tìm cho dễ
        search = search.toLowerCase().replace(" ", "").replace(",", "").replace(".", "").replace(":", "").replace("/", ""); 
        
        // tìm kiếm kết quả
        HashString val = new HashString(search);
        val.setHash();
        int siz2 = val.getStr().length() - 1;

        int dem = 0;
        for(HashString res : lHash)
        {
            News news = listNews.get(dem);
            int siz1 = res.getStr().length() - 1;
            for(int i = 1; i <= siz1 - siz2 + 1; ++i)
            {
                /// kiểm tra xâu val có xuất hiện trong listHash[id] không
                if(res.check(val, i, i + siz2 - 1))
                {
                    ans.add(news);
                    break;
                }
            }
            dem++;
        }
        
        return ans;
    }
    
    // update thanh lăn chuột
    private void updateScrollPane() {
        int preferredHeight = MainPanel.getPreferredSize().height;
        int scrollPaneHeight = ScrollPane.getViewport().getViewSize().height;

        // Kiểm tra nếu kích thước của MainPanel lớn hơn kích thước của JScrollPane
        if (preferredHeight > scrollPaneHeight) {
            // Nếu có, hiển thị thanh cuộn dọc
            ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        } else {
            // Nếu không, ẩn thanh cuộn dọc
            ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }
    }
    // show ra các bài viết
    private static void ContentPanel(ArrayList<News> listNew){
        if (listNew != null && !listNew.isEmpty()){
            for (News news : listNew) {
                JPanel ContentPane = new JPanel();
                ContentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                ContentPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75)); // Thiết lập kích thước tối đa cho panel con
                ContentPane.setLayout(new GridLayout(3, 1)); // GridLayout cho 3 thành phần này
                    
                // Lấy các giá trị từ ArrayList News
                String a = (String) news.getTitle();
                String b = (String) news.getAuthor();
                String c = (String) news.getCreateDate();

                JLabel Baiviet = new JLabel("       "+a);
                JLabel Tacgia = new JLabel("       Tác giả: "+b);
                JLabel Ngay = new JLabel("       Ngày tạo: "+c);
                Baiviet.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Font Arial, kích thước 20
                Dimension nameSize = new Dimension(550, 25);
                Baiviet.setPreferredSize(nameSize);        
                Tacgia.setPreferredSize(nameSize);        
                Ngay.setPreferredSize(nameSize);        
                
                Baiviet.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Tạo JFrame chứa JEditorPane hiển thị thông tin tin tức dưới dạng HTML
                    JFrame outputFrame = new JFrame("Thông tin bài viết");
                    outputFrame.setTitle("Thông tin bài viết");
                    Dimension preferredSize = new Dimension(650, 450);
                    outputFrame.setPreferredSize(preferredSize);
                    outputFrame.setLocation(400, 250); // Đặt vị trí xuất hiện của JFrame

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

                    JEditorPane htmlPane = new JEditorPane();
                    htmlPane.setContentType("text/html");
                    htmlPane.setEditable(false);

                    htmlPane.addHyperlinkListener(a -> {
                 if (a.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                 if (Desktop.isDesktopSupported()) {
                 try {
                Desktop.getDesktop().browse(a.getURL().toURI());
                } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
                  }
                 }
                }
                });
                    String htmlContent = "<html><body>" +                                                 
                        "<p><b>Link bài viết:</b> " + news.getLink() + "</p>" +
                        "<p><b>Nguồn website:</b> " + news.getWebsite() + "</p>" +
                        "<p><b>Loại bài viết:</b> " + news.getTypeBlog() + "</p>" +
                        "<p><b>Tóm tắt bài viết:</b> " + news.getSummary() + "</p>" +   
                        "<p><b>Tiêu đề bài viết:</b> " + a + "</p>" +    
                        "<p><b>Nội dung bài viết:</b> <a href=\"" + news.getLink() + "\">Ấn vào đây</a></p>"+
                        "<p><b>Ngày tạo:</b> " + c + "</p>" +
                        "<p><b>Tag/Hash tag:</b> " + news.getHashTag() + "</p>" +
                        "<p><b>Tác giả:</b> " + b + "</p>" +    
                        "<p><b>Chuyên mục:</b> " + news.getCategory() + "</p>" +
                        "</body></html>";
                    htmlPane.setText(htmlContent);
                    JScrollPane scrollPane = new JScrollPane(htmlPane);

                    // Đặt JScrollPane vào JPanel
                    panel.add(scrollPane, BorderLayout.CENTER);
                    outputFrame.add(panel);
                    outputFrame.pack();
                    outputFrame.setVisible(true);
                    htmlPane.setCaretPosition(0);
                }

                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        // Khi chuột vào, đặt font in đậm và có gạch chân
                        Font font = Baiviet.getFont();
                        Baiviet.setFont(font.deriveFont(font.getStyle() | Font.BOLD ));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        // Khi chuột ra, đặt lại font bình thường
                        Font font = Baiviet.getFont();
                        Baiviet.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD ));
                    }
                });

                ContentPane.add(Baiviet);
                ContentPane.add(Tacgia);
                ContentPane.add(Ngay);
                Content.add(ContentPane);
            } 
        }
        else{
                clearLayout();
                JLabel Nothing = new JLabel("           Không có bài viết cần tìm!");
                Nothing.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Font Arial, kích thước 20   
                Content.add(Nothing);
                // Thêm panel mới vào Content và cập nhật hiển thị
                Content.revalidate();
                Content.repaint();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // phần xử lý khi thao tác Enter hoặc là bấm Button Search
    private void solve() {
        // Xoá màn hình
        clearLayout();
        clearObject();
        try {
            String search = TextForSearch.getText().trim();
            if(!search.equals("")) {
                ArrayList<News> ans = searchSuggestion(search);
                ContentPanel(ans);
                EntityFind(search);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updateScrollPane();
        
        // Thêm panel mới vào Content và cập nhật hiển thị
        Content.revalidate();
        Content.repaint();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TextForSearch = new javax.swing.JTextField();
        Home = new javax.swing.JButton();
        Search = new javax.swing.JButton();
        ScrollPane = new javax.swing.JScrollPane();
        MainPanel = new javax.swing.JPanel();
        Content = new javax.swing.JPanel();
        Trending = new javax.swing.JPanel();
        Object = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Search_API");
        setName("MainFrame"); // NOI18N

        TextForSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextForSearchActionPerformed(evt);
            }
        });

        Home.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Home.setText("Home");
        Home.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HomeActionPerformed(evt);
            }
        });

        Search.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Search.setText("Search");
        Search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchActionPerformed(evt);
            }
        });

        MainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        Content.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout ContentLayout = new javax.swing.GroupLayout(Content);
        Content.setLayout(ContentLayout);
        ContentLayout.setHorizontalGroup(
            ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 596, Short.MAX_VALUE)
        );
        ContentLayout.setVerticalGroup(
            ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        Trending.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout TrendingLayout = new javax.swing.GroupLayout(Trending);
        Trending.setLayout(TrendingLayout);
        TrendingLayout.setHorizontalGroup(
            TrendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 294, Short.MAX_VALUE)
        );
        TrendingLayout.setVerticalGroup(
            TrendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 126, Short.MAX_VALUE)
        );

        Object.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout ObjectLayout = new javax.swing.GroupLayout(Object);
        Object.setLayout(ObjectLayout);
        ObjectLayout.setHorizontalGroup(
            ObjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        ObjectLayout.setVerticalGroup(
            ObjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 610, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout MainPanelLayout = new javax.swing.GroupLayout(MainPanel);
        MainPanel.setLayout(MainPanelLayout);
        MainPanelLayout.setHorizontalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainPanelLayout.createSequentialGroup()
                .addComponent(Content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Trending, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Object, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        MainPanelLayout.setVerticalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(MainPanelLayout.createSequentialGroup()
                .addComponent(Trending, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Object, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        ScrollPane.setViewportView(MainPanel);
        MainPanel.getAccessibleContext().setAccessibleParent(TextForSearch);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(Home)
                .addGap(26, 26, 26)
                .addComponent(TextForSearch)
                .addGap(18, 18, 18)
                .addComponent(Search, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(TextForSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Search)
                        .addComponent(Home)))
                .addGap(18, 18, 18)
                .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName("MFrame");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // trả về dữ liệu mặc định (trang mặc định)
    private void HomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HomeActionPerformed
        // TODO add your handling code here:
        clearLayout();
        clearObject();
        startup();
        updateScrollPane();
        // Thêm panel mới vào Content và cập nhật hiển thị
        Content.revalidate();
        Content.repaint();
    }//GEN-LAST:event_HomeActionPerformed

    private void SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchActionPerformed
        solve();
    }//GEN-LAST:event_SearchActionPerformed

    private void TextForSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextForSearchActionPerformed
        solve();
    }//GEN-LAST:event_TextForSearchActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        setup();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private static javax.swing.JPanel Content;
    private javax.swing.JButton Home;
    private static javax.swing.JPanel MainPanel;
    private static javax.swing.JPanel Object;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JButton Search;
    private javax.swing.JTextField TextForSearch;
    private javax.swing.JPanel Trending;
    // End of variables declaration//GEN-END:variables
}