package com.jsql.util.bruter;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class HashBruter extends Bruter {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    private String hash;
    
    private String generatedHash;
    
    private String password;
    
    private String type;

    public void tryBruteForce() {
        
        this.starttime = System.nanoTime();
        
        for (int size = this.minLength; size <= this.maxLength; size++) {
            
            if (this.found || this.done) {
                break;
            }
            
            while (this.paused) {
                
                try {
                    Thread.sleep(500);
                    
                } catch (InterruptedException e) {
                    
                    LOGGER.error("Interruption while sleeping for brute force", e);
                    Thread.currentThread().interrupt();
                }
            }
            
            try {
                this.generateAllPossibleCombinations(StringUtils.EMPTY, size);
                
            } catch (NoSuchAlgorithmException e) {
                
                LOGGER.error("Coding algorithm not found", e);
                
            } catch (InterruptedException e) {
                
                LOGGER.error("Interruption while generating brute force combinations", e);
                Thread.currentThread().interrupt();
            }
        }
        
        this.done = true;
    }

    private void generateAllPossibleCombinations(String baseString, int length) throws NoSuchAlgorithmException, InterruptedException {
        
        while (this.paused) {
            Thread.sleep(500);
        }
        
        if (!this.found || !this.done) {
            
            if (baseString.length() == length) {
                
                switch (this.type.toLowerCase()) {
                case "adler32": this.generatedHash = HashUtil.toAdler32(baseString); break;
                case "crc16":   this.generatedHash = Crc16.generateCRC16(baseString); break;
                case "crc32":   this.generatedHash = HashUtil.toCrc32(baseString); break;
                case "crc64":   this.generatedHash = Crc64.generateCRC64(baseString.getBytes()); break;
                case "mysql":   this.generatedHash = HashUtil.toMySql(baseString); break;
                case "md4":     this.generatedHash = HashUtil.toMd4(baseString); break;
                default:        this.generatedHash = HashUtil.toHash(this.type, baseString); break;
                }
                
                this.password = baseString;
                
                if (this.hash.equals(this.generatedHash)) {
                    
                    this.found = true;
                    this.done = true;
                }
                
                this.count++;
                
            } else if (baseString.length() < length) {
                
                for (String element: this.characters) {
                    this.generateAllPossibleCombinations(baseString + element, length);
                }
            }
        }
    }
    
    // Getter and setter

    public String getPassword() {
        return this.password;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setType(String digestType) {
        this.type = digestType;
    }

    public String getGeneratedHash() {
        return this.generatedHash;
    }
}
