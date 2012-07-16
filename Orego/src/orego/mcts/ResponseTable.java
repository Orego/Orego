package orego.mcts;

import static orego.core.SuperKoTable.IGNORE_SIGN_BIT;

import java.util.HashMap;

import orego.response.ResponsePlayer;
import orego.util.ListNode;
import orego.util.Pool;

public class ResponseTable extends TranspositionTable {
	/** All hash keys are encoded as levelTwo entries.*/
	private HashMap<Integer, SearchNode> responses;
	
	public ResponseTable(int size, SearchNode prototype) {
		super(0, new SearchNode());
		
		responses = new HashMap<Integer, SearchNode>();
		// dealloc the parent variables (not pretty)
		table = null;
		searchNodes = null;
		listNodes = null;
	}
	
	public ResponseTable(SearchNode prototype) {
		this(0, prototype);
	}
	
	@Override
	protected Pool<SearchNode> getSearchNodes() {
		// no op
		return new Pool<SearchNode>();
	}
	
	@Override
	protected Pool<ListNode<SearchNode>> getListNodes() {
		// no op
		return new Pool<ListNode<SearchNode>>();
	}
	
	@Override
	protected int dagSize(SearchNode root, java.util.Set<SearchNode> visited) {
		return responses.size();
	}
	
	@Override
	public int dagSize(SearchNode root) {
		return responses.size();
	}
	
	
	@Override
	public void addChild(SearchNode parent, SearchNode child) {
		// no op
	}
	
	public void addTwoMoveSequence(int prevPrevMove, int prevMove, int colorToPlay, long boardHash) {
		SearchNode responseList = new SearchNode();
		responseList.reset(boardHash);
		
		responses.put(ResponsePlayer.levelTwoEncodedIndex(prevPrevMove, 
														  prevMove, 
														  colorToPlay), 
														  responseList);
	}
	@Override
	public synchronized SearchNode findIfPresent(long levelTwoIndex) {
		return responses.get((int)levelTwoIndex);
	}
	
	@Override
	public synchronized SearchNode findOrAllocate(long levelTwoIndex) {
		SearchNode found = findIfPresent(levelTwoIndex);
		
		if (found == null) {
			found = new SearchNode();
			responses.put((int)levelTwoIndex, found);
		}
		
		return found;
	}
	
	@Override
	public void sweep() {
		// no op
	}
	
	@Override
	protected void markNodesReachableFrom(SearchNode root) {
		// no op
	}
}
