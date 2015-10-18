package ir.pagerank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Ashish Kalbhor
 *
 * Implementation of Iterative PageRank Algorithm.
 */
public class PageRank 
{
	// DATA STRUCTURES FOR STORING
	public static HashMap<String, HashSet<String>> links = new HashMap<String,HashSet<String>>();
	public static HashMap<String,Double> pr = new HashMap<String,Double>();
	public static HashMap<String,Double> newpr = new HashMap<String,Double>();
	public static HashMap<String, Double> inlinkCounts = new HashMap<String, Double>();
	public static HashMap<String, Integer> outlinkCounts = new HashMap<String, Integer>();
	public static HashSet<String> incomingLinks = new HashSet<String>();
	public static HashSet<String> sinkList = new HashSet<String>();
	public static int sinkNodeCount = 0;
	public static int consecutive = 0;
	public static int noInlinkCount = 0;
	
	/**
	 * Loads all the urls from given WT2g collection.
	 */
	public static void loadPages()
	{
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader("inlinks-input.txt"));
			String line = br.readLine();
			
			while(line != null)
			{
				String[] urlsInLine = line.split(" ");
				HashSet<String> inLinks = new HashSet<String>();
				boolean hasInLinks = false;
				addInOutlinks(urlsInLine[0]);
				
				for(int i = 1; i < urlsInLine.length ; i++)
				{
					hasInLinks = true;
					inLinks.add(urlsInLine[i]);
					incomingLinks.add(urlsInLine[i]);		
				}
				
				if(!hasInLinks)
					noInlinkCount++;
				
				Iterator<String> inlink = inLinks.iterator();
				while(inlink.hasNext())
				{
					addInOutlinks(inlink.next());
				}
				
				
				links.put(urlsInLine[0], inLinks);
				inlinkCounts.put(urlsInLine[0], (double)inLinks.size());
				
				
				line = br.readLine();
			}
			br.close();
			
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
	/**
	 * Adds the given url to outlinkCounts map.
	 */
	public static void addInOutlinks(String url)
	{	
		if(outlinkCounts.containsKey(url))
		{
			outlinkCounts.put(url, outlinkCounts.get(url) + 1);
		}
		else
		{
			outlinkCounts.put(url, 0);
		}
	}
	
	/**
	 * Returns the Perplexity value on distribution of PageRank values,
	 * based on Shannon Entropy.
	 */
	public static Double getPerplexity()
	{
		Double entropy = 0.0;
		Iterator<String> urls = pr.keySet().iterator();
		
		while(urls.hasNext()) 
		{
			String url = urls.next();
		    Double pagerank = pr.get(url);
		    entropy -= pagerank * (Math.log(pagerank) / Math.log(2));
		}
		
		return  Math.pow(2, entropy);
	}
	
	/**
	 * Returns the count of sink nodes present in the graph.
	 */
	public static int getSinkNodesCount()
	{
		Iterator<String> node = links.keySet().iterator();
		while(node.hasNext())
		{
			String parent = node.next();	
			if(incomingLinks.contains(parent))
			{
				continue;
			}
			else
			{
				sinkList.add(parent);
				sinkNodeCount++;
			}
		}
		return sinkNodeCount;
	}
	
	/**
	 * Initializes the PageRank values to 1/N.
	 */
	public static void initPR()
	{
		Set<String> allNodes = links.keySet();
		double totalNodes = allNodes.size();
		
		for(String node : allNodes)
		{
			pr.put(node, 1/totalNodes);
		}
	}
	
	/**
	 * Returns the total SinkPR.
	 */
	public static double calculateTotalSinkPR()
	{
		double sinkPR = 0;
		
		for(String node : sinkList)
		{
			sinkPR += pr.get(node);
		}
		
		return sinkPR;
	}
	
	/**
	 * 
	 * Checks if the PageRank values have been converged.
	 */
	public static boolean isConverged(double oldPerplexity, double newPerplexity)
	{
		// Check if pr list is converged or not.
		if((Math.abs(newPerplexity - oldPerplexity)) < 1 && consecutive == 5)
		{
			return true;
		}
		else if((Math.abs(newPerplexity - oldPerplexity)) < 1)
		{
			consecutive ++;
		}
		return false;
	}
	
	
	/**
	 * The Iterative PageRank Algorithm implementation.
	 */
	public static void genPageRank() throws IOException
	{
		// Iterate till the PageRank has not not converged.
		double sinkprval = 0;
		int size = links.keySet().size();
		int iterations = 0;
		double newPRVal = 0;
		double oldPerplexity = getPerplexity();
		double newPerplexity = oldPerplexity;
		
		BufferedWriter pWriter = new BufferedWriter(new FileWriter("perplexity.txt"));
		
		while(!isConverged(oldPerplexity, newPerplexity))
		{
			sinkprval = 0;
			sinkprval = calculateTotalSinkPR();
			Iterator<String> urls = links.keySet().iterator();
			
			while(urls.hasNext())
			{
				String p = urls.next();
				newPRVal = 0.15 / size;
				newPRVal += (0.85 * sinkprval / size);
				// Processing in-links
				Iterator<String> inLinkList = links.get(p).iterator();
				while(inLinkList.hasNext())
				{
					String q = inLinkList.next();
					newPRVal += (0.85 * pr.get(q) / outlinkCounts.get(q));
				}				
				newpr.put(p, newPRVal);
			}
			
			// Copy newpr to pr
			pr.putAll(newpr);
			oldPerplexity = newPerplexity;
			newPerplexity = getPerplexity();
			iterations++;
			System.out.println("Perplexity => " + newPerplexity);
			pWriter.write("Perplexity at iteration " + iterations + " => " + newPerplexity + "\n");
		}
		pWriter.close();
	}
	
	/**
	 * 
	 * Returns a Sorted TreeMap of the given HashMap.
	 * Used to sort the PageRank values.
	 */
	public static TreeMap<String, Double> SortByValue (HashMap<String, Double> map) 
	{
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
	/**
	 * Removes all the SinkNodes from outlink counts list.	
	 */
	public static void removesinkfromout()
	{
		Iterator<String> urls = outlinkCounts.keySet().iterator();
		while(urls.hasNext())
		{
			String url = urls.next();
			if(outlinkCounts.get(url) == 0)
			{
				urls.remove();
			}
		}
	}
	
	/**
	 * Returns number of pages whose PageRank is less than their 
	 * initial, uniform values.
	 */
	public static int getLessThanInitCount(double n)
	{
		int count = 0;
		Iterator<String> urls = pr.keySet().iterator();
		Double initialValue =  1/n;
		
		while(urls.hasNext())
		{
			String url = urls.next();
			if(pr.get(url) < initialValue)
				count++;
		}
		
		return count;
	}
	
	/**
	 * Main method that calls out all the processes and
	 * 
	 */
	public static void main(String[] args) throws IOException 
	{
		BufferedWriter byPR = new BufferedWriter(new FileWriter("Top PageRank List.txt"));
		BufferedWriter byInlink = new BufferedWriter(new FileWriter("Top InLink Count.txt"));
		
		//////Generating Pre-requisites////////////////
		loadPages();
		System.out.println("Finished loading " + links.size() + " nodes.");
		Double sinkCount = (double)getSinkNodesCount();
		System.out.println("No. of sink nodes:: " + sinkCount);
		initPR();
		System.out.println("Total SinkPR => " + calculateTotalSinkPR());
		//System.out.println("Now writing outlinks in file..");
		//loadOutLinks();
		//loadOutlinksFile();
		removesinkfromout();
		
		System.out.println("Loaded outlinks for " + outlinkCounts.size());
		genPageRank();
		System.out.println("-----------PageRank-----");
		TreeMap<String,Double> sortedPRMap = SortByValue(pr);
		TreeMap<String,Double> sortedILMap = SortByValue(inlinkCounts);
		System.out.println("Top 10 pages are::");
		Iterator<String> urls = sortedPRMap.keySet().iterator();
		int c = 0;
		byPR.write("=====list of the document IDs of the top 50 pages as sorted by PageRank=====\n");
		while(urls.hasNext() && (c < 50))
		{
			c++;
			String url = urls.next();
			System.out.println(url + " : " + pr.get(url));
			byPR.write(url + " => " + pr.get(url) + "\n");			
		}
		
		byInlink.write("=====list of the document IDs of the top 50 pages by in-link count=====\n");
		c = 0;
		urls = sortedILMap.keySet().iterator();
		while(urls.hasNext() && (c < 50))
		{
			c++;
			String url = urls.next();
			byInlink.write(url + " => " + inlinkCounts.get(url) + "\n");			
		}
		byPR.close();
		byInlink.close();
		/////////////////////////////////////////////////////////////////////////////////////
		double totalSize = links.size();
		//3. Proportion of pages with no in-links (sources)
		System.out.println("Proportion of pages with no in-links:: " + 
		noInlinkCount/totalSize);
		
		/////////////////////////////////////////////////////////////////////////////////////
		//4. Proportion of pages with no out-links (sinks)
		System.out.println("Proportion of pages with no out-links:: " + 
		sinkCount/totalSize);
		
		////////////////////////////////////////////////////////////////////////////////////
		//4. Proportion of pages whose PageRank is less than their initial, uniform values.
		System.out.println("Proportion of pages whose PageRank is less than their initial values:: " +
		getLessThanInitCount(totalSize)/totalSize);
	}
}

/**
 * 
 * @author Ashish Kalbhor
 * Comparator class to sort PageRank values.
 *
 */
class ValueComparator implements Comparator<String> {
    Map<String, Double> map;

    public ValueComparator(Map<String, Double> base){
        this.map = base;
    }
 
    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } 
    }
}
