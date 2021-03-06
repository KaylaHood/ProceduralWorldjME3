package worldmanager;

import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import virtualworld.terrain.Point;
import virtualworld.terrain.Terrain;

public class Node {
	
	// finished - Create entity interface for all classes that use WorldManager- 
	// 			  terrain, Sheep, Trees, Towns, Roads, and clouds (Water too?)
	// finished - Make a list of entities that exist within specific node
	// finished - Node should hold center value outside of Terrain, and size
	// finished - items of a given size should be held in node that is big enough to hold object 
	// 			  object's center must exists within node.
	// finished - limit node depth / make minimum size of node

	//Nodes for traversal
	Node parent = null;
	Node[] children = new Node[0];
	
	//data for node
	Point center;
	private double size;
	private int depth;
	private int maxDepth = 40;
	List<Entity> entities = new ArrayList<>();
	
	
	//Constructor
	public Node() {}
	
	//takes Entity as an argument and puts it into the smallest node that can still completely hold the Entity
	public void updateEntites(Entity ent) {
		//Point cent = ent.getCenter();
		//Point currcenter = this.center();
		double sz = ent.getSize();
		if (sz < size/2 && this.getDepth() < maxDepth) {
			if (children.length == 0) {
				this.createChildren();
			}
			for (Node n: children) {
				if(this.checkIfIn(ent.getCenter(), n)) {
					n.updateEntites(ent);
				}
			}
			/*
			if (currcenter.getY() > cent.getY()) {
				if (currcenter.getX() > cent.getX()) {
					children[0].updateEntites(ent);
				}
				else if (currcenter.getX() < cent.getX()) {
					children[1].updateEntites(ent);
				}
			}
			else if (currcenter.getY() < cent.getY()) {
				if (currcenter.getX() > cent.getX()) {
					children[2].updateEntites(ent);
				}
				else if (currcenter.getX() < cent.getX()) {
					children[3].updateEntites(ent);
				}
			}
			*/
		} else {
			entities.add(ent);
		}
	}

	//for checking if an entity's center exists in a Node
	public boolean checkIfIn(Point cent, Node node) {
		double nodeSz = node.getSize();
		Point nodeCent = node.center;
		boolean inBottom = nodeCent.getZ() <= (cent.getZ() + nodeSz/2);
		boolean inRight = nodeCent.getX() <= (cent.getX() + nodeSz/2);
		boolean inLeft = nodeCent.getX() > (cent.getX() - nodeSz/2);
		boolean inTop = nodeCent.getZ() > (cent.getZ() - nodeSz/2);
		return inBottom && inRight && inLeft && inTop;
	}
	
	//updating when connecting to parent or child
	public void updateParent(Node p) {
		parent = p;
	}
	
	//unlikely to be needed, just in case
	public void updateChild(Node[] c) {
		children = c;
	}
	
	//for defining size
	public void updateSize(double sz) {
		size = sz;
	}
	
	//for defining the center point
	public void updateCenter(Point cent) {
		center = cent;
	}
	
	//for defining the depth of the node
	public void updateDepth(int dep) {
		depth = dep;
	}
	
	//function to create Children if called
	public void createChildren() {
		double offset = size/4;
		children = new Node[4];
		for (int i = 0; i < 4; i++) {
			Node newchild = new Node();
			Point newcent;
			if (i == 0) {
				newcent = new Point(this.center.getX()-offset, this.center.getZ()+offset);
			}
			else if (i == 1) {
				newcent = new Point(this.center.getX()+offset, this.center.getZ()+offset);
			}
			else if (i == 2) {
				newcent = new Point(this.center.getX()-offset, this.center.getZ()-offset);
			}
			else {
				newcent = new Point(this.center.getX()+offset, this.center.getZ()-offset);
			}
			newchild.updateSize(size/2);
			newchild.updateParent(this);
			newchild.updateDepth(this.depth+1);
			newchild.updateCenter(newcent);
			children[i] = newchild;
		}
	}
	
	//returns center of Node
	private Point getCenter() {
		return center;
	}
	
	//returns size of Node
	public double getSize() {
		return size;
	}
	
	//returns depth of Node
	public int getDepth() {
		return depth;
	}
	
	//returns a list of all the entities contained within the Node
	public List<Entity> getEntities() {
		return entities;
	}
	
	//Gives all entities distance from camera
	public void cameraDist(Point target) {
		if(children.length > 0) {
			for(Entity e: entities) {
				Point start = e.getCenter();
				double dist = findDist(target,start);
				e.distFromCamera(dist);
			}
			for(Node newnode: children) {
				newnode.cameraDist(target);
			}
		}
	}
	
	//finds the euclidean distance between two points
	public static double findDist(Point target, Point start) {
		double b = Math.abs(start.getZ()-target.getZ());
		double c = Math.abs(start.getX()-target.getX());
		double a = (b*b) + (c*c);
		return Math.sqrt(a);
	}
	
	//finds Terrain that both contains given point and is in an active state and returns height
	public double findHeight(Point target) {
		double height = 0;
		for(Entity e: entities) {
			if(e instanceof Terrain) {
				if(e.isActive()) {
					return ((Terrain) e).getHeightAt(target);
				}
			}
		}
		for (Node n: children) {
			if (checkIfIn(target, n)) {
				height = n.findHeight(target);
			}
		}
		return height;
	}
	
	public List<Terrain> findActiveTerrains() {
		List<Terrain> actives = new ArrayList<>();
		for(Entity e: entities) {
			if(e instanceof Terrain) {
				if(e.isActive()) {
					actives.add((Terrain) e);
				}
			}
		}
		for(Node n: children) {
			actives.addAll(n.findActiveTerrains());
		}
		return actives;
	}
	
	//find out what position of camera uses
	//probably won't need, as cameraDist covers pretty much all cases surrounding camera needs
	public Node findCamera(Point target) {
		Node currNode = this;
		while(currNode.getCenter() != target && currNode.getDepth() < maxDepth) {
			Point currcenter = currNode.getCenter();
			if (currcenter.getZ() > target.getZ()) {
				if (currcenter.getX() > target.getX()) {
					currNode = children[0];
				}
				else if (currcenter.getX() < target.getX()) {
					currNode = children[1];
				}
			}
			else if (currcenter.getZ() < target.getZ()) {
				if (currcenter.getX() > target.getX()) {
					currNode = children[2];
				}
				else if (currcenter.getX() < target.getX()) {
					currNode = children[3];
				}
			}
		}
		return currNode;
	}
}
