import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.OracleDataSource;
import java.io.*;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.SAXReader;


class HCGatherData
{
  public static void main (String args[]) throws Exception
  {
    String jdbc_url = null;
    String v_username = null;
    String v_password = null;
    String v_path = null;
    int v_interval = 2;
    int v_times =10;
    
    // ��������в���û�и�����ʹ�ý���ģʽ����ʾ�û��������
    if (args.length < 1 ){
      try{
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Please input JDBC URL(format:<hostname>:<port>:<sid>):");
      jdbc_url = in.readLine();
      System.out.print("Please input username:");
      v_username = in.readLine();
      System.out.print("Please input password:");
      v_password = in.readLine();
      System.out.print("Please input output directory:");
      v_path = in.readLine();
      System.out.print("\n");          
      }catch(Exception e){e.printStackTrace();} 
     } else if (args.length < 6){  //��������������㣬��ӡ�÷����˳�����
    System.out.println("usage: java HCGatherData <hostname>:<port>:<sid> <username> <password> <output_parent_dir> <interval> <times>");
    System.exit(0);    
    }else {
    	jdbc_url = args[0];
    	v_username = args[1];
    	v_password = args[2];
    	v_path = args[3];
    	v_interval = Integer.parseInt(args[4]);
    	v_times = Integer.parseInt(args[5]);    	
    }
    
            
    OracleDataSource ods = new OracleDataSource();
    
    java.util.Properties prop = new java.util.Properties();

    prop.put("user", v_username);
    prop.put("password", v_password);
        
    //�����sys�û���ָ�����ӵ�ROLE
    if (v_username.toUpperCase().equals("SYS")){
    	prop.put("internal_logon", "SYSDBA");
    	}
    
    ods.setConnectionProperties(prop);
    
    ods.setURL("jdbc:oracle:thin:@"+jdbc_url);
    Connection conn = ods.getConnection();
    System.out.println("Connect OK!");
    
    // Create Oracle DatabaseMetaData object
    DatabaseMetaData meta = conn.getMetaData();

    // gets driver info:
    System.out.println("JDBC driver version is " + meta.getDriverVersion());
                
    //ȡϵͳʱ��
    
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");  //�������ڸ�ʽ
    String dd =df.format(new Date());  // new Date()Ϊ��ȡ��ǰϵͳʱ��   
    String v_instance_name = null;
    String v_host_name = null;
    
    //ȡ��������ʵ����
    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
    ResultSet rset = stmt.executeQuery("select instance_name,host_name from v$instance"); 
    while (rset.next()) {     
                v_instance_name = rset.getString(1);  
                v_host_name = rset.getString(2);   
            }
    rset.close();
    stmt.close();
    
    String v_dir_name = v_host_name+"_"+v_instance_name+"_"+dd;
    System.out.println("Output directory is "+v_dir_name);   
         
    
    File f = new File(v_path+File.separator+v_dir_name) ;// ʵ����File��Ķ���		
    if (f.mkdir()) { ;	// �����ļ���
    System.out.println("Create output directory OK!");
    } else {
    System.out.println("Create output directory fail!");
    System.exit(0);
    }	
    
    String[] parameter_string = new String[3];
    
    parameter_string[2] = v_path +File.separator+v_dir_name; 
    
    System.out.println("Full output directory is "+ parameter_string[2]);
    
    //ȡϵͳ��Ϣ
    try {
			SystemInfo s = new SystemInfo();
							
			Document document1 = DocumentHelper.createDocument();
      Element root1 = document1.addElement("SystemInfo");
                                    
      Element v_hostname = root1.addElement("HostName");
      v_hostname.addText(s.getHostName());
      
      Element v_defaultipaddress = root1.addElement("DefaultIpAddress");
      v_defaultipaddress.addText(s.getDefaultIpAddress());
      
      Element v_cpucount = root1.addElement("CpuCount");      
      String v_cc = s.getCpuCount()+"";
      v_cpucount.addText(v_cc);
      
      Element v_cpuvendor = root1.addElement("CpuVendor");
      v_cpuvendor.addText(s.getCpuVendor());
      
      Element v_cpumodel = root1.addElement("CpuModel");
      v_cpumodel.addText(s.getCpuModel());
      
      Element v_totalmemory = root1.addElement("TotalMemory");
      String v_tm =  s.getTotalMemory()+"";
      v_totalmemory.addText(v_tm.toString());
      
      Element v_totalswap = root1.addElement("TotalSwap");
      String v_ts =  s.getTotalSwap()+"";
      v_totalswap.addText(v_ts.toString());
      
      String[] osinfo = new String[12];
      
      // ȡ����ϵͳ��Ϣ
      osinfo = s.testGetOSInfo();
      
      Element v_arch = root1.addElement("Arch");
      v_arch.addText(osinfo[0]);
      
      Element v_cpuendian = root1.addElement("CpuEndian");
      v_cpuendian.addText(osinfo[1]);
      
      Element v_datamodel = root1.addElement("DataModel");
      v_datamodel.addText(osinfo[2]);
      
      Element v_osdesc = root1.addElement("OSDesc");
      v_osdesc.addText(osinfo[3]);
      
      Element v_machine = root1.addElement("Machine");
      v_machine.addText(osinfo[4]);
      
      Element v_osname = root1.addElement("OSName");
      v_osname.addText(osinfo[5]);
      
      Element v_patchlevel = root1.addElement("PatchLevel");
      v_patchlevel.addText(osinfo[6]);
      
      Element v_osvendor = root1.addElement("OSVendor");
      v_osvendor.addText(osinfo[7]);
      
      Element v_vendorcodename = root1.addElement("VendorCodeName");
      v_vendorcodename.addText(osinfo[8]);
      
      Element v_vendorname = root1.addElement("VendorName");
      v_vendorname.addText(osinfo[9]);
      
      Element v_vendorversion = root1.addElement("VendorVersion");
      v_vendorversion.addText(osinfo[10]);
      
      Element v_osversion = root1.addElement("OSVersion");
      v_osversion.addText(osinfo[11]);
                                       
       //дXML�ļ�
       try{       
            OutputFormat format1 = new OutputFormat("  ", true);
            FileWriter fileout1 = new FileWriter(parameter_string[2]+File.separator+"SYSTEMINFO.xml");
            XMLWriter writer1 = new XMLWriter(fileout1, format1);
            writer1.write(document1);
            fileout1.close();
          }catch(Exception e){   
          e.printStackTrace();
          }
												
		}catch(Exception e){e.printStackTrace();}
    
    //ȡ���ݿ���Ϣ            
    try{
    	        
       //��SQL.xml�ļ�ȡSQL��䲢ִ��
       
       SAXReader saxReaderSql = new SAXReader();
       Document documentSql = saxReaderSql.read("SQL.xml");
       
       Element sqlroot = documentSql.getRootElement();
              
       // iterate through child elements of root
        for ( Iterator i = sqlroot.elementIterator(); i.hasNext(); ) {
            Element row = (Element) i.next();
            
            parameter_string[0] = row.element("SQL_TEXT").getText();
            parameter_string[1] = row.element("SQL_NAME").getText();
            get_result(conn,parameter_string);
          }
       
       // ת��������Alert log�ļ�       
       Statement stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
       ResultSet rset2 = stmt2.executeQuery("select value from v$parameter where name = \'background_dump_dest\'"); 
       
       String v_back_dest = null;
       while (rset2.next()) {       
                v_back_dest = rset2.getString(1);   
            }
       rset2.close();
       stmt2.close();
       
       if (v_back_dest != null){
                     
       String v_log = v_back_dest+File.separator+"alert_"+v_instance_name+".log";
       String v_ht_log = parameter_string[2]+File.separator+"alert_"+v_instance_name+".log";                   
              
                     
       FileReader fr = new FileReader(v_log);   //����һ��FileReader����   �Ӵ��̶�
       BufferedReader br = new BufferedReader(fr);    //����һ��BufferedReader����
       FileWriter fw =new FileWriter(v_ht_log);    //����һ��FileWriter����   д������
       BufferedWriter bw =new BufferedWriter(fw);     //����һ��BufferedReader����
      
       String line = null;                   //����һ��String����line������ÿ�ζ�һ�еĽ��
       
       
      
       while(br.ready()){                             //����ļ�׼���ã��ͼ�����
         line=br.readLine();                          //��һ��
         bw.write(line);                              //дһ��
         bw.newLine();                                //дһ�����з�
         //System.out.println(line);                    //�����
       }
       
       
       bw.flush();                                   //��ջ���
       bw.close();            //�ر���
       fw.close();            //�ر���
       br.close();                                   //�ر���
       fr.close();                                   //�ر���
       
       System.out.print("\n");                         
       System.out.println("Copy alert log file OK!"); 
       }
                            
       }catch(Exception e){e.printStackTrace();} 
     
     
    conn.close();
    
    
     //ȡϵͳ������Ϣ
    try {
			System.out.print("\n");
			System.out.println("System performace information get interval is "+v_interval+" second(s).");
			System.out.println("System performace information get times is "+v_times+".");
			System.out.print("\n");  
			
			SystemInfo ss = new SystemInfo();
							
			Document document2 = DocumentHelper.createDocument();
      Element root2 = document2.addElement("CPUPerf");
                                    
      Document document3 = DocumentHelper.createDocument();
      Element root3 = document3.addElement("MEMPerf");
      
      
      Thread thread = Thread.currentThread();
      
      String[] cpuperf = new String[6];
      String[] memperf = new String[8];
      
      for (int i = 1; i <= v_times; i++)
            {
               System.out.print("\n"); 
               System.out.println("Capture "+i+" :");
               System.out.print("\n"); 
               //cpu
               Element v_row2 = root2.addElement("row");
               cpuperf = ss.getCpuPerc();
               
               Element v_system_time2 = v_row2.addElement("SYSTEM_TIME");
               v_system_time2.addText(df.format(new Date()));
               
               Element v_cpu_user = v_row2.addElement("USER");
               v_cpu_user.addText(cpuperf[0]);
               
               Element v_cpu_sys = v_row2.addElement("SYS");
               v_cpu_sys.addText(cpuperf[1]);
               
               Element v_cpu_wait = v_row2.addElement("WAIT");
               v_cpu_wait.addText(cpuperf[2]);
               
               Element v_cpu_nice = v_row2.addElement("NICE");
               v_cpu_nice.addText(cpuperf[3]);
               
               Element v_cpu_idle = v_row2.addElement("IDLE");
               v_cpu_idle.addText(cpuperf[4]);
               
               Element v_cpu_total = v_row2.addElement("TOTAL");
               v_cpu_total.addText(cpuperf[5]);
               
               //mem
               Element v_row3 = root3.addElement("row");
               memperf = ss.getPhysicalMemory();
               
               Element v_system_time3 = v_row3.addElement("SYSTEM_TIME");
               v_system_time3.addText(df.format(new Date()));
               
               Element v_mem_total = v_row3.addElement("MEM_TOTAL");
               v_mem_total.addText(memperf[0]);
               
               Element v_mem_used = v_row3.addElement("MEM_USED");
               v_mem_used.addText(memperf[1]);
               
               Element v_mem_free = v_row3.addElement("MEM_FREE");
               v_mem_free.addText(memperf[2]);
               
               Element v_swap_total = v_row3.addElement("SWAP_TOTAL");
               v_swap_total.addText(memperf[3]);
               
               Element v_swap_used = v_row3.addElement("SWAP_USED");
               v_swap_used.addText(memperf[4]);
               
               Element v_swap_free = v_row3.addElement("SWAP_FREE");
               v_swap_free.addText(memperf[5]);
               
               Element v_pagein = v_row3.addElement("PAGEIN");
               v_pagein.addText(memperf[6]);
               
               Element v_pageout = v_row3.addElement("PAGEOUT");
               v_pageout.addText(memperf[7]);
               
               
               
               thread.sleep(v_interval*1000);//��ͣN���������ִ��
                    
            }
                                           
             
       //дXML�ļ�
       try{       
            OutputFormat format2 = new OutputFormat("  ", true);
            FileWriter fileout2 = new FileWriter(parameter_string[2]+File.separator+"CPUPerf.xml");
            XMLWriter writer2 = new XMLWriter(fileout2, format2);
            writer2.write(document2);
            fileout2.close();
            
             OutputFormat format3 = new OutputFormat("  ", true);
            FileWriter fileout3 = new FileWriter(parameter_string[2]+File.separator+"MEMPerf.xml");
            XMLWriter writer3 = new XMLWriter(fileout3, format3);
            writer3.write(document3);
            fileout3.close();
            
          }catch(Exception e){   
          e.printStackTrace();
          }
												
		}catch(Exception e){e.printStackTrace();}
		
		//�ļ�ϵͳ��Ϣ
		
		try {
									
			SystemInfo ss = new SystemInfo();
							
			Document document4 = DocumentHelper.createDocument();
      Element root4 = document4.addElement("FILESYSTEM_INFO");
      
      System.out.print("\n");
			System.out.println("Local filesystem information:");
      System.out.print("\n");  
                                          
      String[][] v_fs = ss.getFileSystemInfo();
      
      System.out.print("\n");
			System.out.println("Local filesystem count: "+v_fs.length);
      System.out.print("\n");  
            
      
      for (int i = 0; i < v_fs.length; i++)
            {
               
               
               if (v_fs[i][5].equals("2"))
               {
               	Element v_row4 = root4.addElement("row");
               	Element v_fs_dev = v_row4.addElement("FS_DEV");
               	v_fs_dev.addText(v_fs[i][0]);
               	
               	Element v_fs_dir = v_row4.addElement("FS_DIR");
               	v_fs_dir.addText(v_fs[i][1]);
               	
               	Element v_fs_flag = v_row4.addElement("FS_FLAG");
               	v_fs_flag.addText(v_fs[i][2]);
               	
               	Element v_fs_systypename = v_row4.addElement("FS_SYSTYPENAME");
               	v_fs_systypename.addText(v_fs[i][3]);
               	
               	Element v_fs_typename = v_row4.addElement("FS_TYPENAME");
               	v_fs_typename.addText(v_fs[i][4]);
               	
               	Element v_fs_type = v_row4.addElement("FS_TYPE");
               	v_fs_type.addText(v_fs[i][5]);
               	
               	Element v_fs_usage_total= v_row4.addElement("FS_USAGE_TOTAL");
               	v_fs_usage_total.addText(v_fs[i][6]);
               	
               	Element v_fs_usage_free = v_row4.addElement("FS_USAGE_FREE");
               	v_fs_usage_free.addText(v_fs[i][7]);
               	
               	Element v_fs_usage_av = v_row4.addElement("FS_USAGE_AV");
               	v_fs_usage_av.addText(v_fs[i][8]);
               	
               	Element v_fs_usage_used = v_row4.addElement("FS_USAGE_USED");
               	v_fs_usage_used.addText(v_fs[i][9]);
               	
               	Element v_fs_usage_per = v_row4.addElement("FS_USEAGE_PER");
               	v_fs_usage_per.addText(v_fs[i][10]);
               	
              }
                                             
            }
                                           
             
       //дXML�ļ�
       try{       
            OutputFormat format4 = new OutputFormat("  ", true);
            FileWriter fileout4 = new FileWriter(parameter_string[2]+File.separator+"filesystem_info.xml");
            XMLWriter writer4 = new XMLWriter(fileout4, format4);
            writer4.write(document4);
            fileout4.close();
            
            
          }catch(Exception e){   
          e.printStackTrace();
          }
												
		}catch(Exception e){e.printStackTrace();}
			
		//��û���������Ϣ
		
		try {
			
			
			SystemInfo ss = new SystemInfo();
							
			Document document5 = DocumentHelper.createDocument();
      Element root5 = document5.addElement("ENV");
      
      Element v_oracle_home = root5.addElement("ORACLE_HOME");
      v_oracle_home.addText(System.getenv("ORACLE_HOME"));  
      
                                             
                         
       //дXML�ļ�
       try{       
            OutputFormat format5 = new OutputFormat("  ", true);
            FileWriter fileout5 = new FileWriter(parameter_string[2]+File.separator+"env.xml");
            XMLWriter writer5 = new XMLWriter(fileout5, format5);
            writer5.write(document5);
            fileout5.close();
                        
          }catch(Exception e){   
          e.printStackTrace();
          }
			
		  System.out.print("\n"); 
      System.out.println("Get System Env Var OK!");
      System.out.print("\n");  
      							
		}catch(Exception e){e.printStackTrace();}
		
        
    //���ռ���XML���
    ZipUtils.createZip(v_path+File.separator+v_dir_name, v_path+File.separator+v_dir_name+".zip");
    
      System.out.print("\n"); 
      System.out.println("Compress data OK!");
      System.out.print("\n");  
      System.out.println("Gather data completed!");
    
   }
    
   public static void get_result (Connection arg_conn,String[] parameter_string) throws Exception {  
    
    Statement stmt = arg_conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                
    System.out.println("\nExecuting query: " + parameter_string[0]);
    System.out.print("\n");
    
    ResultSet rset = stmt.executeQuery(parameter_string[0]); 
    ResultSetMetaData rsmd = rset.getMetaData();
    
    String v_name = null;
    String v_value = null;
    
    int colCount = rsmd.getColumnCount();
    
             	
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(parameter_string[1]);
    
    
    
    //ȡ�����������XML��ʽ��� 
    if (!rset.next()){
       	Element v_row = root.addElement("row");
       	v_row.addText("##null##");
      }else {
      
       	do 
     {     
                                    
            Element v_row = root.addElement("row");
            for (int i = 1; i <= colCount; i++)
            {
                String columnName = rsmd.getColumnName(i);
                Object o_value      = rset.getObject(i);  
                Element node  = v_row.addElement(columnName);
                if (o_value == null){o_value="";};
                node.addText(o_value.toString());
                if (System.getenv("HTHC_OUTPUT")!=null) {
                System.out.print("\""+o_value.toString()+"\""+",");   
              }  
            }
            if (System.getenv("HTHC_OUTPUT")!=null) {
            System.out.print("\n");    
          }
       }while (rset.next());
     }     
            
     
       //дXML�ļ�
       try{       
            OutputFormat format = new OutputFormat("  ", true);
            format.setEncoding("GBK");
            FileWriter fileout = new FileWriter(parameter_string[2]+File.separator+parameter_string[1]+".xml");
            XMLWriter writer = new XMLWriter(fileout, format);
            writer.write(document);
            fileout.close();
          }catch(Exception e){   
          e.printStackTrace();
          }
       	    
    rset.close();
    stmt.close();
    
   }
}