import java.util.concurrent.ConcurrentLinkedQueue;

public class TreeRecogStrat extends RecogStrategy{

	private Node root;
	private ConcurrentLinkedQueue<DescContainer> lq = new ConcurrentLinkedQueue<>();
	
	private int size=0;


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
			if(!current.full())
			{
				current.add(new Node(id));
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
			
			double score = in.compareSURF(current.d.descData);
			if(score>max)
			{
				max=score;
				maxn=current;
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
		if(max>threshhold)
		{
			return new MatchResult(maxn.d.stringData,max);
		}
		return null;
	}

	public synchronized void finalizeLoad()
	{
		System.out.println("loading");
		while(lq.size()>0)
		{
			if(lq.size()%100==0)
			{
				System.out.println(lq.size());
			}
			process(lq.remove());
		}
		System.out.println("loaded!");
	}

	private class Node
	{
		DescContainer d;
		Node left;
		Node right;
		double s;

		public Node(DescContainer d)
		{
			this.d = d;
		}

		public boolean full()
		{
			if(left==null || right==null)
			{
				return false;
			}
			return true;
		}
		
		public void add(Node n)
		{
			if(left==null)
			{
				left=n;
			}
			else if(right==null)
			{
				right=n;
			}
		}
		
		public Node max(ImageDesc d)
		{
			Node m = null;
			double max = 0;

			if(left != null)
			{
				double score = d.compareSURF(left.d.descData);
				if(score>max)
				{
					max=score;
					m = left;
				}
			}
			
			if(right != null)
			{
				double score = d.compareSURF(right.d.descData);
				if(score>max)
				{
					max=score;
					m = right;
				}
			}

			s = max;
			return m;
		}

		public Node maxHash(ImageDesc d)
		{
			Node m = null;
			double max = 0;

			if(left != null)
			{
				double score = d.compareHash(left.d.descData);
				if(score>max)
				{
					max=score;
					m = left;
				}
			}
			
			if(right != null)
			{
				double score = d.compareHash(right.d.descData);
				if(score>max)
				{
					max=score;
					m = right;
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
