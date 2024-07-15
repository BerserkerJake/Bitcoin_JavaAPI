package main;
import info.blockchain.api.blockexplorer.*;
import info.blockchain.api.blockexplorer.entity.*;
import info.blockchain.api.*;
import java.lang.*;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;


public class DatasetGenerator {
	String file;

	public DatasetGenerator(String file) {
		this.file = file;
	}

	public boolean writeTransactions() {
		// TODO implement me

		try {
			FileWriter fw = new FileWriter(file, true);	
			BlockExplorer blockExpl = new BlockExplorer();
			List<Block> clients = new ArrayList<>();

			for (long l = 265852L; l <= 266085L; l++) {
				List<Block> results = blockExpl.getBlocksAtHeight(l);
				for (Block b: results) {
					clients.add(b);
				}
			}
		

			for (Block c: clients) {
				List<Transaction> allTx = c.getTransactions();
			
				for (Transaction t: allTx) {
					int runThruAllOutputs = 0;
					String txHash = t.getHash();
					List<Input> allInputsPerTx = t.getInputs();
	
					for (Input i: allInputsPerTx) {
						if (i.getPreviousOutput().getValue() != 0) {
							String inputAddr = i.getPreviousOutput().getAddress();
							Long inputVal = i.getPreviousOutput().getValue();
							String inputRecord = generateInputRecord(txHash, inputAddr, inputVal);

							fw.write(inputRecord + '\n');

							if (runThruAllOutputs == 0) { // once we found a non-coinbase Tx, we run thru all outputs;
														  // this loop only runs once per non-coinbase Tx
								List<Output> allOutputsPerTx = t.getOutputs();
								for (Output o: allOutputsPerTx) {
									String outputAddr = o.getAddress();
									Long outputVal = o.getValue();
									String outputRecord = generateOutputRecord(txHash, outputAddr, outputVal);

									fw.write(outputRecord + '\n');
								}
							}
							runThruAllOutputs = 1; 
						}
					}
				}
			}

			fw.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;

	}

	/**
	 * Generate a record in the transaction dataset
	 *
	 * @param txHash
	 *            Transaction hash
	 * @param address
	 *            Previous output address of the input
	 * @param value
	 *            Number of Satoshi transferred
	 * @return A record of the input
	 */
	private String generateInputRecord(String txHash,
			String address, long value) {
		return txHash + " " + address + " " + value + " in";
	}

	/**
	 * Generate a record in the transaction dataset
	 *
	 * @param txHash
	 *            Transaction hash
	 * @param address
	 *            Output bitcoin address
	 * @param value
	 *            Number of Satoshi transferred
	 * @return A record of the output
	 */
	private String generateOutputRecord(String txHash,
			String address, long value) {
		return txHash + " " + address + " " + value + " out";
	}
}
