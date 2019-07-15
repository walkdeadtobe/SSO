package com.aak;

import com.aak.api.OauthController;
import com.zaxxer.hikari.HikariDataSource;
import org.omg.CORBA.BAD_CONTEXT;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.commons.logging.*;
import javax.sql.DataSource;
import java.io.*;


@SpringBootApplication
@Configuration
//@EnableAutoConfiguration
//@ComponentScan
public class AuthorizationServerApplication {

	public static Log log= LogFactory.getLog(AuthorizationServerApplication.class);

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource mainDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	public static void main(String[] args) {
		getbcryt();
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
		/*String back = passwordEncoder.encode("root");
		log.info(back);
		back = passwordEncoder.encode("normal");
		log.info(back);

	    back = passwordEncoder.encode("zhiku");
		log.info(back);
		back = passwordEncoder.encode("normal");
		log.info(back);*/
		String back = passwordEncoder.encode("test");
		log.info(back);
	}
}
