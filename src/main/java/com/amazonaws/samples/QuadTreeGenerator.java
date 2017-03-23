package com.amazonaws.samples;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/**
 * 
 * @author shawnkoon
 * @Cite github.com/shawnkoon
 * @version 1.0
 *
 *	Total number of nodes : Sum ( 4^[n] ) 
 *	n = Exponent from (xAxis || yAxis).convert(base = 2)
 *
 *	Current Functionality.
 *	- xAxis && yAxis needs to be same to form a Square.
 *	- xAxis && yAxis needs to be Power of 2.
 *	- Uses recursive way to create quad tree.
 */

public class QuadTreeGenerator {
	/*
	 * Tree related fields.
	 */
	public static long xAxis;
	public static long yAxis;
	public static long nodeNumber;
	
	/*
	 * AWS related fields.
	 */
	public static AmazonDynamoDBClient dynamoDB;
	public static String tableName;
	
	/*
	 * Initialize basic fields.
	 */
	public static void init() {
		xAxis = 4;
		yAxis = 4;
		nodeNumber = 0;
		
		// AWS setup.
		tableName = "QuadTree";
		
		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~\\.aws\\credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
	}
	
	/*
	 * Check to see if program should get executed or not.
	 */
	public static void checkForExit() {
		if (xAxis > 0 && yAxis > 0) {
			if(xAxis == yAxis) {
				if (isPowerTwo(xAxis)) {
					if(!isPowerTwo(yAxis)) {
						System.out.println(" Y-axis needs to be power of 2.");
						System.exit(-1);
					}
				}
				else {
					System.out.println(" X-axis needs to be power of 2.");
					System.exit(-1);
				}							
			}
			else {
				System.out.println(" X-axis & Y-axis needs to be equal.");
				System.exit(-1);
			}
		}
		else {
			System.out.println(" X-Axis and Y-Axis needs to be positive Number.");
			System.exit(-1);
		}
	}
	
	/**
	 * Function to see if passed in param is power of 2 or not.
	 * 
	 * @param number: number to be checked.
	 * @return [true] if it is power 2. Else [false].
	 */
	private static boolean isPowerTwo(long number) {
		return (number & (number - 1)) == 0;
	}
	
	/*
	 * Creates DynamoDB table 
	 */
	public static void createTable() {
		try {

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName("node").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("node").withAttributeType(ScalarAttributeType.N))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            System.out.println("Table "+ tableName +" Creating... Please Wait...");
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, tableName);
            System.out.println("Table "+ tableName +" Creation Finished.");
            
            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            System.exit(-1);
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            System.exit(-1);
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Creates newItem object with nodeNumber and coordinates.
	 * 
	 * @param nodeNumber: Number of node index.
	 * @param coordinates: coordinate in format of `(Xmin,Ymin,Xmax,Ymax)`
	 * @return newItem object to be inserted on to table.
	 */
	private static Map<String, AttributeValue> newItem(String nodeNumber, String coordinates) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("node", new AttributeValue().withN(nodeNumber));
        item.put("coordinates", new AttributeValue().withS(coordinates));

        return item;
    }
	
	/**
	 * Create QuadTree using recursive Method.
	 * 
	 * @param xMin: Minimum x of the box.
	 * @param yMin:	Minimum y of the box.
	 * @param xMax: Maximum x of the box.
	 * @param yMax: Maximum y of the box.
	 * 
	 */
	public static void createTree(long xMin, long yMin, long xMax, long yMax) {
		
		String coordinates = "(" + xMin + "," + yMin + "," + xMax + "," + yMax + ")";
		System.out.println("Node : " + nodeNumber + " "+ coordinates);
		
		try {
			Map<String, AttributeValue> item = newItem(nodeNumber + "", coordinates);
			PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
			dynamoDB.putItem(putItemRequest);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		nodeNumber++;

		// Base case.
		if((xMax - xMin) == 1 || (yMax - yMin) == 1) {
		}
		// Recursive case.
		else {	
			// Left up
			createTree(xMin, (yMax - yMin)/2 + yMin, (xMax - xMin)/2  + xMin, yMax);
			// Right up
			createTree((xMax - xMin)/2 + xMin, (yMax - yMin)/2 + yMin, xMax, yMax);
			// Left down
			createTree(xMin, yMin, (xMax - xMin)/2 + xMin, (yMax - yMin)/2 + yMin);
			// Right down
			createTree((xMax - xMin)/2 + xMin, yMin, xMax, (yMax - yMin)/2 + yMin);
		}
	}
}