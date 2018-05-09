import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TreeRecogStrat extends RecogStrategy{

	private Node root;
	private ConcurrentLinkedQueue<DescContainer> lq = new ConcurrentLinkedQueue<>();
	
	private int size=0;

	private static final int BRANCH_FACTOR = 2;


	public synchronized void clear()
	{
		root = null;
		size=0;
	}
	
	public synchronized void add(DescContainer dc)
	{
		lq.add(dc);
		size++;
	}

	public synchronized void process(DescContainer id)
	{
		Node current = root;

		if(root == null)
		{
			root = new Node(id);
			return;
		}

		for(;;)
		{
			if(current.next.size() < BRANCH_FACTOR)
			{
				current.next.add(new Node(id));
				return;
			}
			else
			{
				current = current.maxHash(id.descData);
			}
		}
	}

	public synchronized MatchResult getMatch(ImageDesc in, double threshhold)
	{

		Node current = root;
		Node maxn = root;
		double max = 0;

		for(;;)
		{
			if(current == null)
			{
				break;
			}

			if(current == root)
			{
				double score = in.compareSURF(current.d.descData);
				if(score>max)
				{
					max=score;
					maxn=current;
				}
			}

			if(current.next.size()==0)
			{
				double score = in.compareSURF(current.d.descData);
				if(score>max)
				{
					max=score;
					maxn=current;
				}
				break;
			}
			else
			{
				Node n = current.max(in);
				if(current.s>max)
				{
					max=current.s;
					maxn=n;
				}
				current = n;
			}
		}
		//		if(max>threshhold)
		//		{
		//			System.out.println(i);
		//			return new MatchResult(maxn.d.stringData,max);
		//		}
		if(root!=null)
		{
			ArrayList<DescContainer> sim = getSimilar(maxn.d.descData,root,0.85);
			DescContainer d = mostSimilar(maxn.d.descData,sim);



			if(d!=null&&d.match>threshhold)
			{
				return new MatchResult(d.stringData,d.match);
			}
		}
		return null;
	}

	public synchronized DescContainer mostSimilar(ImageDesc i, ArrayList<DescContainer> sim)
	{
		double max = 0;
		DescContainer m = null;
		for(DescContainer d:sim)
		{
			double score = d.descData.compareSURF(i);
			if(score>max)
			{
				max=score;
				m=d;
			}
		}
		if(m!=null)
		{
			m.match=max;
		}
		return m;
	}

	public synchronized ArrayList<DescContainer> getSimilar(ImageDesc i,Node r,double thresh)
	{
		ArrayList<DescContainer> sim = new ArrayList<DescContainer>();
		if(r.next.size()==0)
		{
			if(i.compareHash(r.d.descData)>=thresh)
			{
				sim.add(r.d);
			}
		}
		else
		{
			for(Node next:r.next)
			{
				sim.addAll(getSimilar(i,next,thresh));
			}
		}
		return sim;
	}

	public synchronized void finalizeLoad()
	{
		System.out.println("loading");
		while(lq.size()>0)
		{
			process(lq.remove());
		}
		System.out.println("loaded!");
	}

	private class Node
	{
		DescContainer d;
		LinkedList<Node> next;
		double s;

		public Node(DescContainer d)
		{
			this.d = d;
			next = new LinkedList<>();
		}

		public Node max(ImageDesc d)
		{
			Node m = null;
			double max = 0;

			for(Node n:next)
			{
				double score = d.compareSURF(n.d.descData);
				if(score>max)
				{
					max=score;
					m = n;
				}
			}

			s = max;
			return m;
		}

		public Node maxHash(ImageDesc d)
		{
			Node m = null;
			double max = 0;

			for(Node n:next)
			{
				double score = d.compareHash(n.d.descData);
				if(score>max)
				{
					max=score;
					m = n;
				}
			}
			s = max;
			return m;
		}
	}

	public synchronized int size() {
		return size;
	}
}
