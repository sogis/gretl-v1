package ch.so.agi.gretl.util;

import java.io.IOException;

/**
 * Class which reads the given sql-statements
 */
public class SqlReader {

    public static String readSqlStmt(java.io.PushbackReader reader)
            throws IOException {
        StringBuffer stmt=new StringBuffer();
        int c=reader.read();
        while(c!=-1){
            if(c=='-'){
                c=reader.read();
                if(c==-1){
                    stmt.append('-');
                    break;
                }else if(c=='-'){
                    c=reader.read();
                    while(c!=-1){
                        if(c=='\n'){
                            c=reader.read();
                            if(c!=-1 && c!='\r'){
                                reader.unread(c);
                            }
                            break;
                        }else if(c=='\r'){
                            c=reader.read();
                            if(c!=-1 && c!='\n'){
                                reader.unread(c);
                            }
                            break;
                        }
                        c=reader.read();
                    }
                }else{
                    stmt.append('-');
                    stmt.append((char)c);
                }
            }else if(c=='\''){
                stmt.append((char)c);
                while(true){
                    c=reader.read();
                    if(c==-1){
                        break;
                    }else if(c=='\''){
                        c=reader.read();
                        if(c==-1){
                            // eof
                            break;
                        }else if(c=='\''){
                            stmt.append('\'');
                            stmt.append('\'');
                        }else{
                            reader.unread(c);
                            break;
                        }
                    }else{
                        stmt.append((char)c);
                    }
                }
                stmt.append('\'');
            }else if(c==';'){
                stmt.append((char)c);
                // skip end of line
                c=reader.read();
                if(c=='\n'){
                    c=reader.read();
                    if(c!=-1 && c!='\r'){
                        reader.unread(c);
                    }
                }else if(c=='\r'){
                    c=reader.read();
                    if(c!=-1 && c!='\n'){
                        reader.unread(c);
                    }
                }else{
                    if(c!=-1){
                        reader.unread(c);

                    }
                }
                break;
            }else if(c=='\n'){
                stmt.append(" ");
                c=reader.read();
                if(c!=-1 && c!='\r'){
                    reader.unread(c);
                }
            }else if(c=='\r'){
                stmt.append(" ");
                c=reader.read();
                if(c!=-1 && c!='\n'){
                    reader.unread(c);
                }
            }else{
                stmt.append((char)c);
            }
            c=reader.read();
        }
        if(stmt.length()==0){
            return null;
        }
        return stmt.toString();
    }

}
