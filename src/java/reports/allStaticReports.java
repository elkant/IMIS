/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reports;

import database.dbConn;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Geofrey Nyabuto
 */
public class allStaticReports extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
    response.setContentType("text/html;charset=UTF-8");
    
    //a page to get Report of all the servlets
    
    String year="2015";
    String month="";
    String county="";
    String form="moh731";
    
    
    if(request.getParameter("year")!=null){
    year=request.getParameter("year");
    }
    
    
    if(request.getParameter("county")!=null){
    county=request.getParameter("county");
    }
    
    if(request.getParameter("month")!=null){
    month=request.getParameter("month");
    }
    
     if(request.getParameter("form")!=null){
    form=request.getParameter("form");
    }
    
   String facilitywhere="";
   String countywhere="";
   String districtwhere="";
   String reporttype="";
    dbConn conn= new dbConn();
    
    
    
    
    
    
    //an array to store haeder information.
    
    //the header information should appear only if a certain parameters are met
    //The parameters listed in here can be removed if the report type doesnt require certain parameters
    ArrayList Headerorgunits=new ArrayList();
    Headerorgunits.add("COUNTY");
    Headerorgunits.add("SUB-COUNTY");
    Headerorgunits.add("FACILITY");
    Headerorgunits.add("MFL CODE");
   
    
    
    //An arralist to store a list of columns that will be selected from the database
    ArrayList dbcolumns=new ArrayList();
    
    ArrayList labels=new ArrayList();
    
    ArrayList tablename=new ArrayList();
    
    ArrayList iscumulative=new ArrayList();    
    
    ArrayList ispercent=new ArrayList();
    
    
   // ArrayList isactive=new ArrayList();
     //An arralist to store a list of worksheets that will be selected from the sections
    ArrayList worksheets=new ArrayList();
    //An arralist to store distinct worksheets. This will be derived from the the sections column
    ArrayList distinctsheets=new ArrayList();
    
    String selectdistinctworksheet="select distinct(section) from pivottable where form='"+form+"' and active='1'";
    
    conn.rs=conn.st.executeQuery(selectdistinctworksheet);
    
    while(conn.rs.next()){
        //add the name of distinct sections
    distinctsheets.add(conn.rs.getString(1).replace("/", "_"));
  
    
    
                         }
    
    String getattribs="select indicator,label,section,cumulative,percentage,active ,shortlabel from pivottable where form='"+form+"' order by tableid, section";
    conn.rs=conn.st.executeQuery(getattribs);     
    
    while(conn.rs.next()){
        
        //add active indicators only
        
        if(conn.rs.getString("active").equals("1")){
    System.out.println(conn.rs.getString("indicator")+"");
    //add indicator
    dbcolumns.add(conn.rs.getString("indicator"));
    //add label
    if(form.equals("moh731")){
         labels.add(conn.rs.getString("shortlabel")+" \n "+conn.rs.getString("label"));
   
    }
    else {
     labels.add(conn.rs.getString("label"));
    }
    //add worksheets
    worksheets.add(conn.rs.getString("section").replace("/", "_"));
    
    
    String perc="0";
    String cum="0";
    

    
        if (conn.rs.getString("cumulative") != null) {
            iscumulative.add(conn.rs.getString("cumulative"));
        } else {
            iscumulative.add(cum);
        }

    
        if (conn.rs.getString("percentage") != null) {
            ispercent.add(conn.rs.getString("percentage"));
        } else {
            ispercent.add(perc);
        }
        
        }//end of active 
        
      }  //end of worksheet
    
    //if
    
    String perfacilselect="select   Upper(County) as County , Upper(DistrictNom) as District , UPPER(SubPartnerNom) as facility ,CentreSanteId as mflcode , ";

  
    //--------------------------------------------------------------------------------------------
    //             PREPARE SELECT
    //--------------------------------------------------------------------------------------------
    //prepare selects
    
    for(int a=0;a<dbcolumns.size();a++){
    
     //if the indicator is a percent, get an avaerage
        
        if(ispercent.get(a).equals("1")){
         perfacilselect+="  AVG("+dbcolumns.get(a)+") as "+dbcolumns.get(a);
        
        }
        else   if(iscumulative.get(a).equals("1")){
         perfacilselect+="  "+dbcolumns.get(a)+" as "+dbcolumns.get(a);
        
        }
        
        else {
         perfacilselect+="  SUM("+dbcolumns.get(a)+") as "+dbcolumns.get(a);
        
        }
        
        //if the item is not the last, append a comma
        
       if(a<dbcolumns.size()-1){
       
           perfacilselect+=" ,";
       
       } 
        
    
    }
    
  //------------------------------------------------------------------------------------
  //     FROM  
  //------------------------------------------------------------------------------------  
  
 perfacilselect+=" from "+form+"  join ( subpartnera join (district join county on county.CountyID=district.CountyID ) on district.DistrictID = subpartnera.DistrictID )  on "+form+".SubPartnerID = subpartnera.SubPartnerID ";   
  
 
//------------------------------------------------------------------------------------------
// WHERE 
//------------------------------------------------------------------------------------------ 
 
 
 
 
 
 
 
 
 //-----------------------------------------------------------------------------------------
 //GROUP BY 
 //----------------------------------------------------------------------------------------
 
 perfacilselect+=" group by subpartnera.SubPartnerID";
 
 
            System.out.println(perfacilselect);
  //______________________________________________________________________________________
  //                       NOW CREATE THE WORKSHEETS          
  //______________________________________________________________________________________  
            
              XSSFWorkbook wb=new XSSFWorkbook();
              
              
              
              
    //______________________________________________________________________________________
    //______________________________________________________________________________________
              
            XSSFFont font = wb.createFont();
            font.setFontHeightInPoints((short) 18);
            font.setFontName("Cambria");
            font.setColor((short) 0000);
            CellStyle style = wb.createCellStyle();
            style.setFont(font);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            XSSFFont font2 = wb.createFont();
            font2.setFontName("Cambria");
            font2.setColor((short) 0000);
            CellStyle style2 = wb.createCellStyle();
            style2.setFont(font2);
            style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
            style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
            style2.setAlignment(HSSFCellStyle.ALIGN_LEFT);

            XSSFCellStyle stborder = wb.createCellStyle();
            stborder.setBorderTop(HSSFCellStyle.BORDER_THIN);
            stborder.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            stborder.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            stborder.setBorderRight(HSSFCellStyle.BORDER_THIN);
            stborder.setAlignment(HSSFCellStyle.ALIGN_CENTER);

            XSSFCellStyle stylex = wb.createCellStyle();
            stylex.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            stylex.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            stylex.setBorderTop(HSSFCellStyle.BORDER_THIN);
            stylex.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            stylex.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            stylex.setBorderRight(HSSFCellStyle.BORDER_THIN);
            stylex.setAlignment(HSSFCellStyle.ALIGN_LEFT);

            XSSFFont fontx = wb.createFont();
            fontx.setColor(HSSFColor.BLACK.index);
            fontx.setFontName("Cambria");
            stylex.setFont(fontx);
            stylex.setWrapText(true);

   
    
  
              

   for(int b=0;b<distinctsheets.size();b++){
     XSSFSheet shet=wb.createSheet(distinctsheets.get(b).toString().toUpperCase());   
     
    //create headers for that worksheet
      
      XSSFRow rw=shet.createRow(1);
      int headercellpos=0;
      //create the orgunit header eg COUNTY | SUBCOUNTY  | FACILITY
      
       for(int e=0;e<Headerorgunits.size();e++){
       XSSFCell cell0=rw.createCell(headercellpos);
       cell0.setCellValue(Headerorgunits.get(e).toString());
       cell0.setCellStyle(stylex);
       headercellpos++;
       shet.setColumnWidth(e, 6000);         
                                               }
       
    
         
       //create the indicators header eg HV0101 | HIV 09676  | TOTAL    
     for (int c=0;c<dbcolumns.size();c++)
     {
       //compare if the indicator belongs to the specified section and hence worksheet 
         //recall, each indicator has got an associated section / worksheet
         //An indicator should be put as an header in the respective worksheet
         if(worksheets.get(c).equals(distinctsheets.get(b))){
             
                 shet.setColumnWidth(headercellpos, 6000);  
       XSSFCell cell0=rw.createCell(headercellpos);
       cell0.setCellValue(labels.get(c).toString());
       cell0.setCellStyle(stylex);
       headercellpos++;
                                                              }//end of comparing if
     
     }//end of for loop
   
                                            }         
            
    conn.rs=conn.st.executeQuery(perfacilselect);
    String sectioncopy="";
   
    int sheetpos=0;
    int rowpos=2;
    
    while(conn.rs.next()){
    //-----------------INSIDE THE DATA FORM---------------------------------
    //if the section changes, change the position of the worksheet too
    //also, reset the position counter to begin from 2 again. 
        
      XSSFSheet shet=null;
      
//      if(--!sectioncopy.equals(shet)){}
        
      
      
     for(int g=0;g<distinctsheets.size();g++){
       shet=wb.getSheetAt(g);  
        int colpos=0;
        //the fourth cell should     
      XSSFRow rw=shet.createRow(rowpos);
     for(int e=0;e<Headerorgunits.size();e++){
       XSSFCell cell0=rw.createCell(colpos);
       cell0.setCellValue(conn.rs.getString(e+1));
       cell0.setCellStyle(style2);
       colpos++;        
       
                                               }
     
     //_________________________________________________________________
     //VALUES
     //_________________________________________________________________
     
      //create the indicators values eg 90 | 45  | 356    
        for (int c = 0; c < dbcolumns.size(); c++) {
         //get the section of the current dbcolumn
                      
            //compare if the indicator belongs to the specified section and hence worksheet 
            //recall, each indicator has got an associated section / worksheet
            //An indicator should be put as an header in the respective worksheet
            if (worksheets.get(c).equals(distinctsheets.get(g))) {

                XSSFCell cell0 = rw.createCell(colpos);
                cell0.setCellValue(conn.rs.getString(dbcolumns.get(c).toString()));
                cell0.setCellStyle(stborder);
                colpos++;
            }//end of comparing if

        }//end of for loop
     
     
     
     
     }
     
        
     
     
     rowpos++;
     
    
    
    
    }
            
            
   
    
ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
wb.write(outByteStream);
byte [] outArray = outByteStream.toByteArray();
response.setContentType("application/ms-excel");
response.setContentLength(outArray.length);
response.setHeader("Expires:", "0"); // eliminates browser caching
response.setHeader("Content-Disposition", "attachment; filename="+form.toUpperCase()+"_REPORT.xlsx");
OutputStream outStream = response.getOutputStream();
outStream.write(outArray);
outStream.flush();
}       catch (SQLException ex) {
            Logger.getLogger(allStaticReports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
