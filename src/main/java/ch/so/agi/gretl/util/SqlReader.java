package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.io.*;

/**
 * Class which reads the given sql-statements
 */
public class SqlReader {
    private static FileInputStream sqlFileInputStream;
    private static InputStreamReader sqlFileReader;
    private static PushbackReader reader;

    private static GretlLogger log = LogEnvironment.getLogger(SqlReader.class);

    
    public static String readSqlStmt(File sqlfile)
            throws IOException {

        createPushbackReader(sqlfile);

        StringBuffer stmt=new StringBuffer();
        int c=reader.read();

        stmt = createStatement(c,reader,stmt);

        if(stmt.length()==0){
            return null;
        }

        return stmt.toString();

    }

    public static void createPushbackReader(File sqlfile) throws FileNotFoundException {
        sqlFileInputStream = new FileInputStream(sqlfile);
        sqlFileReader = new InputStreamReader(sqlFileInputStream);

        reader = new PushbackReader(sqlFileReader);
    }

    private static StringBuffer createStatement(int c, java.io.PushbackReader reader, StringBuffer stmt)
            throws IOException{

        while(c!=-1) {
            if (c!=';') {
                stmt = handlingGivenCharacters(c,reader,stmt);
                c=reader.read();
            } else {
                c = reader.read();
                break;
            }

        }
        return stmt;
    }


    private static StringBuffer handlingGivenCharacters(int c, java.io.PushbackReader reader, StringBuffer stmt)
            throws IOException{

        switch ((char) c) {
            case '-':
                stmt = checkCharacterAfterHyphen(reader,stmt);
                break;
            case '\'':
                stmt = addingQuotedString(c, reader, stmt);
                break;
            case ';':
                stmt = splitStatement(c, reader, stmt);
                break;
            case '\n':
                stmt = replaceLineBreakCharacter(c, reader, stmt);
                break;
            case '\r':
                stmt = replaceLineBreakCharacter(c, reader, stmt);
                break;
            default:
                stmt.append((char) c);
                break;
        }
        return stmt;
}

private static StringBuffer checkCharacterAfterHyphen(java.io.PushbackReader reader, StringBuffer stmt )
        throws IOException{

    int c=reader.read();
    switch (c) {
        case -1:
            stmt.append('-');
            break;
        case '-':
            ignoreCommentsUntilLinebreak(reader);
            break;
        default:
            stmt.append('-');
            stmt.append((char) c);
            break;
    }
    return stmt;
}


    private static void ignoreCommentsUntilLinebreak(java.io.PushbackReader reader)
            throws IOException {

        int c=reader.read();
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
    }


    private static StringBuffer addingQuotedString(int c, java.io.PushbackReader reader, StringBuffer stmt)
            throws IOException{

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
        return stmt;
    }

    private static StringBuffer splitStatement(int c, java.io.PushbackReader reader, StringBuffer stmt)
            throws IOException {

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
        return stmt;
    }

    private static StringBuffer replaceLineBreakCharacter(int c, java.io.PushbackReader reader, StringBuffer stmt)
            throws IOException {

        if(c=='\n'){
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
        }
        return stmt;
    }

    public static String nextSqlStmt() throws IOException{

        StringBuffer stmt=new StringBuffer();
        int c=reader.read();
        while (c==' ') {
            c = reader.read();
        }

        stmt = createStatement(c,reader,stmt);

        if(stmt.length()==0){
            closePushbackReader();
            return null;
        }

        return stmt.toString();
    }


    public static void closePushbackReader()
            throws IOException {
        sqlFileReader.close();
        sqlFileInputStream.close();
    }


}

