package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Math;
import java.util.*;

public class UserCluster {
	private Map<Long, List<String>> userMap; // Map a user id to a list of
												// bitcoin addresses
	private Map<String, Long> keyMap; // Map a bitcoin address to a user id

	int hash_index = 0;
	int addr_index = 1;
	int val_index  = 2;
	int type_index = 3;
	int column_len = 4;

	long userID = 0L;
	List<List<String>> allTxRecords;

	public UserCluster() {
		userMap = new HashMap<Long, List<String>>();
		keyMap = new HashMap<String, Long>();
		allTxRecords = new ArrayList<>();

	}

	/**
	 * Read transactions from file
	 *
	 * @param file
	 * @return true if read succeeds; false otherwise
	 */
	public boolean readTransactions(String file) {
		// TODO implement me

		
		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			List<String> allRecordsPerTx = new ArrayList<>();
			String newLine = "";
			String readTxHash = ""; // last txHash just read
			int counter = 0;

			while ((newLine = br.readLine()) != null) {
				//System.out.println(newLine);
				String[] s = newLine.split(" ");
				String txHash = s[hash_index];

				if (readTxHash.equals("")) { // adding the first record for all transactions
					allRecordsPerTx.add(newLine);
					//System.out.println(allRecordsPerTx);
					readTxHash = txHash;
				}

				else if (txHash.equals(readTxHash)) { // this means we are still reading the same transaction 	
					allRecordsPerTx.add(newLine);
					readTxHash = txHash; // add all records that belong to the same transaction
				}

				else if (!txHash.equals(readTxHash)) { //this means we are done with all records associated with one single transaction
					allTxRecords.add(new ArrayList<>(allRecordsPerTx)); // add all records for a single transaction to the final result
					allRecordsPerTx.clear();
					allRecordsPerTx.add(newLine);
					readTxHash = txHash;
				} 

			}

			allTxRecords.add(new ArrayList<>(allRecordsPerTx)); // add the last transaction to the final records
			// System.out.println(readTxHash);
			// System.out.println(allTxRecords);
			
			return true; 

			//for (int i = 0; i < 5; i++) {
			//	System.out.println(allTxRecords.get(i));
			//}

			/*
			for (List<String> perTx: allTxRecords) {
				for (String ss: perTx) {
					System.out.println(ss);
					String[] s = ss.split(" ");
					String addr = s[addr_index];
					String recordType = s[type_index];
					
					System.out.println(addr);
					System.out.println(recordType);
					break;
				}
				break;
			} */

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false; 
	} 

	/**
	 * Merge addresses based on joint control
	 */
	public void mergeAddresses() {
		// TODO implement me

			for (List<String> allRecordsPerTx: allTxRecords) {
				long jcUser = -123L;
				boolean ifJcUserPresent = false;

				for (String singleRecordPerTx: allRecordsPerTx) {
					String[] s = singleRecordPerTx.split(" ");
					String addr = s[addr_index];
					String recordType = s[type_index];

					if (recordType.equals("in") && (keyMap.containsKey(addr))) {
						jcUser = keyMap.get(addr); // use the first existing in-type record's user ID as the join control per transaction
						ifJcUserPresent = true;
						break;
					}
				}
			
				
				for (String singleRecordPerTx: allRecordsPerTx) {
					//System.out.println(singleRecordPerTx);
					String[] s = singleRecordPerTx.split(" ");
					String addr = s[addr_index];
					String recordType = s[type_index];

					if (recordType.equals("in") && keyMap.containsKey(addr) && keyMap.get(addr) != jcUser) {
					//if (!userMap.get(jcUser).contains(addr)) { userMap.get(jcUser).add(addr); }
						long removeID = keyMap.get(addr);
						for (String userAddr: userMap.get(removeID)) {
							if (userMap.containsKey(jcUser)) {
								userMap.get(jcUser).add(userAddr);
							}
							else {
								userMap.computeIfAbsent(jcUser, k -> new ArrayList<>()).add(userAddr);
							}
							
							keyMap.put(userAddr, jcUser);
						}
						userMap.remove(removeID);
					}

					else if (recordType.equals("in") && !keyMap.containsKey(addr) && ifJcUserPresent) {
						//if (!userMap.get(jcUser).contains(addr)) { userMap.get(jcUser).add(addr); }
						keyMap.put(addr, jcUser);
						if (userMap.containsKey(jcUser)) {
								userMap.get(jcUser).add(addr);
						}
						else {
							userMap.computeIfAbsent(jcUser, k -> new ArrayList<>()).add(addr);
						}
					}

					else { // case when no need to merge

						if (recordType.equals("in") && !keyMap.containsKey(addr)) {	
							long inputID = userID;
							userID++;
							keyMap.put(addr, inputID);
							if (userMap.containsKey(inputID)) {
								userMap.get(inputID).add(addr);
							}
							else {
								userMap.computeIfAbsent(inputID, k -> new ArrayList<>()).add(addr);
							}
							
						}

						if (recordType.equals("out") && !keyMap.containsKey(addr)) {
							long outputID = userID;
							userID++;	
							keyMap.put(addr, outputID);
							if (userMap.containsKey(outputID)) {
								userMap.get(outputID).add(addr);
							}
							else {
								userMap.computeIfAbsent(outputID, k -> new ArrayList<>()).add(addr);
							}
						}
					}
				}
			}
	}
		


	/**
	 * Return number of users (i.e., clusters) in the transaction dataset
	 *
	 * @return number of users (i.e., clusters)
	 */
	public int getUserNumber() {
		// TODO implement me

		return userMap.size();
	}

	/**
	 * Return the largest cluster size
	 *
	 * @return size of the largest cluster
	 */
	public int getLargestClusterSize() {
		// TODO implement me

		int maxClusterSize = -1;

		for (Map.Entry<Long, List<String>> item: userMap.entrySet()) {
			int currSize = item.getValue().size();
			if (currSize > maxClusterSize) {
				maxClusterSize = currSize;
			}
		}

		return maxClusterSize; 

	}

	public boolean writeUserMap(String file) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(file));
			for (long user : userMap.keySet()) {
				List<String> keys = userMap.get(user);
				w.write(user + " ");
				for (String k : keys) {
					w.write(k + " ");
				}
				w.newLine();
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			System.err.println("Error in writing user list!");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean writeKeyMap(String file) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(file));
			for (String key : keyMap.keySet()) {
				w.write(key + " " + keyMap.get(key) + "\n");
				w.newLine();
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			System.err.println("Error in writing key map!");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean writeUserGraph(String txFile, String userGraphFile) {
	     try {
                        BufferedReader r1 = new BufferedReader(new FileReader(txFile));
                        Map<String, Long> txUserMap = new HashMap<String, Long>();
                        String nextLine;
                        while ((nextLine = r1.readLine()) != null) {
                                String[] s = nextLine.split(" ");
                                if (s.length < column_len) {
                                        System.err.println("Invalid format: " + nextLine);
                                        r1.close();
                                        return false;
                                }
                                if (s[type_index].equals("in") && !txUserMap.containsKey(s[hash_index])) { // new transaction
                                        Long user;
                                        if ((user=keyMap.get(s[addr_index])) == null) {
                                                System.err.println(s[addr_index] + " is not in the key map!");
                                                System.out.println(nextLine);
                                                r1.close();
                                                return false;
                                        }
                                        txUserMap.put(s[hash_index], user);
                                }
                        }
                        r1.close();

                        BufferedReader r2 = new BufferedReader(new FileReader(txFile));
                        BufferedWriter w = new BufferedWriter(new FileWriter(userGraphFile));
                        while ((nextLine = r2.readLine()) != null) {
                                String[] s = nextLine.split(" ");
                                if (s.length < column_len) {
                                        System.err.println("Invalid format: " + nextLine);
                                        r2.close();
                                        w.flush();
                                        w.close();
                                        return false;
                                }
                                if (s[type_index].equals("out")) {
                                        if(txUserMap.get(s[hash_index]) == null) {
                                                System.err.println("Did not find input transaction for Tx: " + s[hash_index]);
                                                r2.close();
                                                w.flush();
                                                w.close();
                                                return false;
                                        }
                                        long inputUser = txUserMap.get(s[hash_index]);
                                        Long outputUser;
                                        if ((outputUser=keyMap.get(s[addr_index])) == null) {
                                                System.err.println(s[addr_index] + " is not in the key map!");
                                                r2.close();
                                                w.flush();
                                                w.close();
                                                return false;
                                        }
                                        w.write(inputUser + "," + outputUser + "," + s[val_index] + "\n");
                                }
                        }
                        r2.close();
                        w.flush();
                        w.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return true;

	}

	public void analyzeUserGraph() {

		Map<String, Long> receiverDict = new HashMap<>();
		Map<String, Long> senderDict = new HashMap<>();
		Map<String, List<String>> rtosDict = new HashMap<>();
		int sending_addr_index = 0;
		int receiving_addr_index = 1;
		int satoshi_index = 2; 

		String FBIId = null;
		Long maxReceiverVal = Long.MIN_VALUE;

		try(BufferedReader br = new BufferedReader(new FileReader("userGraph.txt"))){
			String nextLine;

			while((nextLine = br.readLine()) != null){
				String[] s = nextLine.split(",");
				String sender = s[sending_addr_index];
				String receiver = s[receiving_addr_index];
				String value = s[satoshi_index];

				List<String> senderList = rtosDict.getOrDefault(receiver, new ArrayList<>());
				senderList.add(sender);
				rtosDict.put(receiver, senderList);

				if(receiverDict.containsKey(receiver)) {
					receiverDict.put(receiver, receiverDict.get(receiver) + Long.parseLong(value));
				}

				else {
					receiverDict.put(receiver, Long.parseLong(value));
				}

				if(senderDict.containsKey(sender)) {
					senderDict.put(sender, senderDict.get(sender) + Long.parseLong(value));
				}
				
				else {
					senderDict.put(sender, Long.parseLong(value));
				}
				
				if(maxReceiverVal < receiverDict.get(receiver)){
					maxReceiverVal = receiverDict.get(receiver);
					FBIId = receiver;
				}
			}

			List<String> fbiAddr = userMap.get(Long.parseLong(FBIId));
			System.out.println("FBI Address: " + fbiAddr);
			long maxSenderVal = -1L;
			String maxSender = null;

			for(String sender: rtosDict.get(FBIId)){

				if(maxSenderVal < senderDict.get(sender)){
					maxSenderVal = senderDict.get(sender);
					maxSender = sender;
				}
			}

			List<String> maxSenderAddr = userMap.get(Long.parseLong(maxSender));
			System.out.println("Potential silk road address: " + maxSenderAddr.get(0) + " " + maxSenderAddr.get(1) + " " + maxSenderAddr.get(2));
			//System.out.println("111");

		} catch (Exception e){
			e.printStackTrace();
		}
	} 
}
