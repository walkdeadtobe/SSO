package com.aak;

import com.aak.api.OauthController;
import com.zaxxer.hikari.HikariDataSource;
import org.omg.CORBA.BAD_CONTEXT;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.commons.logging.*;
import javax.sql.DataSource;
import java.io.*;


@ServletComponentScan
@SpringBootApplication
@EnableCaching
@Configuration
//@EnableAutoConfiguration
//@ComponentScan
public class AuthorizationServerApplication {

	public static Log log= LogFactory.getLog(AuthorizationServerApplication.class);

	/*@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource mainDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}
	 */

	public static void main(String[] args) {
		//getbcryt();
		SpringApplication.run(AuthorizationServerApplication.class, args);
	}
	public static void filereader(){
		BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
		/*String back = passwordEncoder.encode("root");
		log.info(back);
		back = passwordEncoder.encode("normal");
		log.info(back);

	    back = passwordEncoder.encode("zhiku");
		log.info(back);
		back = passwordEncoder.encode("normal");
		log.info(back);
		back = passwordEncoder.encode("test");
		log.info(back);*/
		try {
			//read files
			FileReader reader=new FileReader("/home/sun/Downloads/username_password.txt");
			BufferedReader br = new BufferedReader(reader);

			// write files
			File  writername=new File("/home/sun/Downloads/data.txt");
			writername.createNewFile();
			FileWriter writer=new FileWriter(writername);
			BufferedWriter out= new BufferedWriter(writer);
			String read_line,write_line;
			String[]back;
			int i=8;
			while ((read_line=br.readLine())!=null){
				log.info(read_line);
				back=read_line.split(":");
				write_line= "INSERT INTO credentials  VALUES("+String.valueOf(i)+",b'1','"+back[0]+"','"+passwordEncoder.encode(back[1])+"','DEPART','0');\r\n";
				out.write(write_line);
				i++;

			}
			br.close();
			reader.close();
			for(int j=8;j<i;j++)
			{
				write_line= "INSERT INTO credentials_authorities VALUE ("+String.valueOf(j)+",6);\r\n";
				out.write(write_line);
			}
			out.flush();
			out.close();
			writer.close();

		}catch (IOException e){
			e.printStackTrace();
		}

	}
	public static void getbcryt() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		/*String back="";
		String print="";
		int start=556;
		//INSERT INTO credentials  VALUES(542,b'1','test','$2a$10$Rfh93hckGzL.8gG8oT1CfODwxNurJ8OHqpPcvtUNQGsG0IGeSBmIq','DEPART','0');
		for(int i=1;i<=20;i++) {
			back = passwordEncoder.encode("test" + i);
			back="INSERT INTO credentials  VALUES("+(start+i)+",b'1','"+"test" + i+"','"+back+"','DEPART','0');";
			print+=back+"\n";
		}
		log.info(print);
		print="";
		for(int i=1;i<=20;i++) {
			back="INSERT INTO credentials_authorities VALUE ("+(start+i)+",6);";
			print+=back+"\n";
		}
		log.info(print)；
		 */
		String back = passwordEncoder.encode("fresfgrc9");
		log.info(back);
		back = passwordEncoder.encode("ffwer32f");
		log.info(back);
		back = passwordEncoder.encode("sf6awdg");
		log.info(back);
		back = passwordEncoder.encode("dsf82ki2");
		log.info(back);
		back = passwordEncoder.encode("1njfwe9");
		log.info(back);
		back = passwordEncoder.encode("3fsdfew8");
		log.info(back);
		back = passwordEncoder.encode("2vdfs92");
		log.info(back);
		back = passwordEncoder.encode("few22rfs");
		log.info(back);
/*
	    back = passwordEncoder.encode("zhiku");
		log.info(back);
		back = passwordEncoder.encode("normal");
		log.info(back);
		String back = passwordEncoder.encode("inskygtj");
		log.info(back);
		back = passwordEncoder.encode("r3skyg9h");
		log.info(back);*/
	}
}
