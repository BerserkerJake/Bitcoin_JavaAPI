package main;
import info.blockchain.api.blockexplorer.*;
import info.blockchain.api.blockexplorer.entity.*;
import info.blockchain.api.*;
import java.lang.*;

import java.util.*;
import java.io.IOException;

public class Checkpoint1 {
	Block client;
	String hashCode;

	public Checkpoint1() {

		this.hashCode = "000000000000000f5795bfe1de0381a44d4d5ea2ad81c21d77f275bffa03e8b3";
		BlockExplorer blockExpl = new BlockExplorer();
		try {
			this.client = blockExpl.getBlock(this.hashCode);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * Blocks-Q1: What is the size of this block?
	 *
	 * Hint: Use method getSize() in Block.java
	 *
	 * @return size of the block
	 */
	public long getBlockSize() {
		// TODO implement me
		return client.getSize();
	}

	/**
	 * Blocks-Q2: What is the Hash of the previous block?
	 *
	 * Hint: Use method getPreviousBlockHash() in Block.java
	 *
	 * @return hash of the previous block
	 */
	public String getPrevHash() {
		// TODO implement me
		return client.getPreviousBlockHash();
	}

	/**
	 * Blocks-Q3: How many transactions are included in this block?
	 *
	 * Hint: To get a list of transactions in a block, use method
	 * getTransactions() in Block.java
	 *
	 * @return number of transactions in current block
	 */
	public int getTxCount() {
		// TODO implement me
		return client.getTransactions().size();
	}

	/**
	 * Transactions-Q1: Find the transaction with the most outputs, and list the
	 * Bitcoin addresses of all the outputs.
	 *
	 * Hint: To get the bitcoin address of an Output object, use method
	 * getAddress() in Output.java
	 *
	 * @return list of output addresses
	 */
	public List<String> getOutputAddresses() {
		// TODO implement me

		List<Transaction> allTx = client.getTransactions();
		int outputCount = 0;
		List<Output> mostOutputs = new ArrayList<>();
		List<String> outputAddr = new ArrayList<>(); 

		for (Transaction t: allTx){
			List<Output> outputPerTx = t.getOutputs();
			if (outputCount < outputPerTx.size()) {
				outputCount = outputPerTx.size();
				mostOutputs = outputPerTx;
			}
		}

		for (Output o: mostOutputs) {
			outputAddr.add(o.getAddress());
		}

		return outputAddr;
	}

	/**
	 * Transactions-Q2: Find the transaction with the most inputs, and list the
	 * Bitcoin addresses of the previous outputs linked with these inputs.
	 *
	 * Hint: To get the previous output of an Input object, use method
	 * getPreviousOutput() in Input.java
	 *
	 * @return list of input addresses
	 */
	public List<String> getInputAddresses() {
		// TODO implement me

		List<Transaction> allTx = client.getTransactions();
		int inputCount = 0;
		List<Input> mostInputs = new ArrayList<>();
		List<String> inputAddr = new ArrayList<>();

		for (Transaction t: allTx) {
			List<Input> inputPerTx = t.getInputs();
			if (inputCount < inputPerTx.size()) {
				inputCount = inputPerTx.size();
				mostInputs = inputPerTx;
			}
		}

		for (Input i: mostInputs) {
			if (i.getPreviousOutput().getValue() != 0) {
				inputAddr.add(i.getPreviousOutput().getAddress());
			}
		}

		return inputAddr;
	}

	/**
	 * Transactions-Q3: What is the bitcoin address that has received the
	 * largest amount of Satoshi in a single transaction?
	 *
	 * Hint: To get the number of Satoshi received by an Output object, use
	 * method getValue() in Output.java
	 *
	 * @return the bitcoin address that has received the largest amount of Satoshi
	 */
	public String getLargestRcv() {
		// TODO implement me

		List<Transaction> allTx = client.getTransactions();
		long maxSatoshi = 0L;
		List<Output> maxSatoshiOutput = new ArrayList<>();
		String bitcoinAddr = new String();

		for (Transaction t: allTx) {
			List<Output> outputPerTx = t.getOutputs();
			for (Output o: outputPerTx) {
				if (maxSatoshi < o.getValue()) {
					maxSatoshi = o.getValue();
					maxSatoshiOutput.add(o);
				}
			}
		}

		return maxSatoshiOutput.get(maxSatoshiOutput.size() - 1).getAddress();
	}

	/**
	 * Transactions-Q4: How many coinbase transactions are there in this block?
	 *
	 * Hint: In a coinbase transaction, getPreviousOutput() == null --> although this matches with the documentation, the result is wrong.
	 * Even if it's a coinbase transactions, it's not null during my test.
	 * I would recommend another work around that a coinbase transaction should have the sum of getPreviousOutput().getValue() equal to 0 because the total input should be 0.
	 * You can see an example of coinbase transaction here: https://www.blockchain.com/btc/tx/cdab676fe718b5155251f15b275c5f92ad965ee8557270d1eec07ccc42d4aaaf
	 * I'm using Java 1.8.0_242, if anyone made it success with getPreviousOutput() == null, please email me or send a campuswire post. Much appreciated!
	 *
	 * @return number of coin base transactions
	 */
	public int getCoinbaseCount() {
		// TODO implement me
		
		int totalCoinbase = 0;

		List<Transaction> allTx = client.getTransactions();
		for (Transaction t: allTx) {
			List<Input> inputPerTx = t.getInputs();
			for (Input i: inputPerTx) {
				if (i.getPreviousOutput().getValue() == 0) {
					totalCoinbase += 1;
				}
			}
		}

		return totalCoinbase;
	}

	/**
	 * Transactions-Q5: What is the number of Satoshi generated in this block?
	 *
	 * @return number of Satoshi generated
	 */
	public long getSatoshiGen() {
		// TODO implement me

		List<Transaction> allTx = client.getTransactions();
		long satoshiSum = 0L;

		for (Transaction t: allTx) {
			List<Input> inputPerTx = t.getInputs();
			for (Input i: inputPerTx) {
				if (i.getPreviousOutput().getValue() == 0) {
					List<Output> outputPerTx = t.getOutputs();
					for (Output o: outputPerTx) {
						satoshiSum += o.getValue();
					}
				}
			}
		}

		return satoshiSum;
	}

}
