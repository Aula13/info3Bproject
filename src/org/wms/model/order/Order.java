package org.wms.model.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Order model
 * Include the business logic
 * Include hibernate annotations for persistence
 * 
 * @author Stefano Pessina, Daniele Ciriello
 *
 */
@Entity
@Table(name="wms_order")
public class Order {
	
	@Id
	@Column(name="order_id")
	private long id;
	
	@Column(name="emission_date", nullable=false)
	private Date emissionDate;
	
	@Column(name="prioriry", nullable=false)
	private Priority priority = Priority.LOW;
	
	/**
	 * List of the OrderRow that this order contains
	 */
	@OneToMany(fetch=FetchType.EAGER, mappedBy="order", cascade=CascadeType.REMOVE)
	private List<OrderRow> rows = new ArrayList<>();
	
	@Column(name="order_type", nullable=false)
	private OrderType type; 
	
	private OrderStatus orderStatus = OrderStatus.WAITING;
	
	private float completePercentual = 0.0f;
	
	private float allocationPercentual = 0.0f;
	
	@Column(name="done_date")
	private Date doneDate;
	
	public Order() {
	}
	
	
	
	public Order(long id) {
		this(id,
			new Date(),
			OrderType.INPUT,
			Priority.LOW);
	}



	public Order(long id, Date emissionDate, OrderType orderType) {
		super();
		this.id = id;
		this.emissionDate = emissionDate;
		this.type = orderType;
	}

	public Order(long id, Date emissionDate, OrderType orderType, Priority priority) {
		this(id, emissionDate, orderType);
		this.priority = priority;
	}

	public Order(long id, Date emissionDate, OrderType orderType, Priority priority,
			List<OrderRow> rows) {
		this(id, emissionDate, orderType, priority);
		this.rows = rows;
	}

	/**
	 * @return order id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Set order id
	 * if the order is new (id==0)
	 * @param order id
	 * @return true=id updated
	 */
	public boolean setId(long id) {
		if(id!=0l)
			return false;
		this.id = id;
		return true;
	}
	
	/**
	 * check if all the data are provided for the order
	 * getId isn't 0
	 * emission date is valid
	 * 
	 * @return true = the data are complete 
	 */
	public boolean isDataComplete() {
		if(getId()==0l)
			return false;
		
		if(getEmissionDate()==null)
			return false;
		return true;
	}

	/**
	 * @return emission date
	 */
	public Date getEmissionDate() {
		return emissionDate;
	}
	
	/**
	 * Set the emission date
	 * if the order is editable
	 * @param emissionDate 
	 * @return true=emission date updated
	 * 
	 * @see org.wms.model.order.Order#isEditable()
	 */
	public boolean setEmissionDate(Date emissionDate) {
		if(!isEditable())
			return false;
		this.emissionDate = emissionDate;
		return true;
	}
	
	/**
	 * @return get order type
	 */
	public OrderType getType() {
		return type;
	}

	/**
	 * @return get order priority
	 */
	public Priority getPriority() {
		return priority;
	}
	
	/**
	 * Set order priority
	 * if the order is editable
	 * 
	 * @param priority 
	 * @return true=priority changed, false = order not editable
	 * 
	 * @see org.wms.model.Order#isEditable()
	 */
	public boolean setPriority(Priority priority) {
		if(!isEditable())
			return false;
		this.priority = priority;
		return true;
	}

	/**
	 * @return unmodificable list of orderrow
	 */
	public List<OrderRow> getUnmodificableMaterials() {
		return Collections.unmodifiableList(rows);
	}
	
	/**
	 * Add a material to the order
	 * if the order is editable
	 * and if the orderrow data is complete
	 * 
	 * @param orderRow
	 * @return true=material is added
	 * 
	 * @see org.wms.model.OrderRow#isDataComplete()
	 * @see org.wms.model.Order#isEditable()
	 */
	public boolean addMaterial(OrderRow orderRow) {
		if(!isEditable() || !orderRow.isDataComplete())
			return false;
		rows.add(orderRow);
		updateAllocatedPercentual();
		updateCompletedPercentual();
		return false;
	}
	
	/**
	 * Remove a material to the order
	 * if the order is editable
	 * 
	 * @param orderRow
	 * @return true=material is removed
	 * 
	 * @see org.wms.model.Order#isEditable()
	 */
	public boolean removeMaterial(OrderRow orderRow) {
		if(!isEditable())
			return false;
		rows.remove(orderRow);
		updateAllocatedPercentual();
		updateCompletedPercentual();
		return true;
	}

	/**
	 * @return order status
	 */
	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	/**
	 * @return order complete percentual
	 */
	public float getCompletePercentual() {
		return completePercentual;
	}

	/**
	 * @return order allocation percentual
	 */
	public float getAllocationPercentual() {
		return allocationPercentual;
	}

	/**
	 * @return done date
	 */
	public Date getDoneDate() {
		return doneDate;
	}
	
	/**
	 * Set a material as allocated
	 * 
	 * after material is marked as allocated,
	 * allocated percentual of the order will be updated
	 * 
	 * @param material allocated
	 */
	public void setMaterialAsAllocated(Material material) {
		setMaterialAsAllocated(material.getCode());
	}
	
	/**
	 * Set a material orderrow as allocated
	 * 
	 * after material is marked as allocated,
	 * allocated percentual of the order will be updated
	 * 
	 * @param materialId of the orderrow allocated
	 */
	public void setMaterialAsAllocated(long materialId) {
		Optional<OrderRow> rowToAllocate = rows.stream()
			.filter(row -> row.getMaterial().getCode()==materialId)
			.findFirst();
		
		if(rowToAllocate.isPresent()) {
			rowToAllocate.get().setAllocated();		
			updateAllocatedPercentual();
		}
	}
	
	/**
	 * Set a material orderrow as completed
	 * 
	 * after material is marked as completed,
	 * completed percentual of the order will be updated
	 * 
	 * @param material completed
	 */
	public void setMaterialAsCompleted(Material material) {
		setMaterialAsCompleted(material.getCode());
	}
	
	/**
	 * Set a material orderrow as completed
	 * 
	 * after material is marked as completed,
	 * completed percentual of the order will be updated
	 * 
	 * @param materialId of the orderrow completed
	 */
	public void setMaterialAsCompleted(long materialId) {
		Optional<OrderRow> rowToComplete = rows.stream()
			.filter(row -> row.getMaterial().getCode()==materialId)
			.findFirst();
		
		if(rowToComplete.isPresent()) {
			rowToComplete.get().setCompleted();
			updateCompletedPercentual();
		}
	}
	
	/**
	 * update the allocated percentual
	 * of the order
	 */
	public void updateAllocatedPercentual() {
		if(rows.size()==0) {
			allocationPercentual=0.0f;
			return;
		}
		
		int allocatedOrderRowSize = rows.stream()
				.filter(row -> row.isAllocated())
				.collect(Collectors.toList()).size();
		allocationPercentual = (((float) allocatedOrderRowSize)/rows.size())*100;
	}
	
	/**
	 * update the completed percentual
	 * of the order
	 */
	public void updateCompletedPercentual() {
		if(rows.size()==0) {
			completePercentual=0.0f;
			return;
		}
		
		int compleOrderRowRowSize = rows.stream()
				.filter(row -> row.isCompleted())
				.collect(Collectors.toList()).size();
		completePercentual = (((float) compleOrderRowRowSize)/rows.size())*100;
	}
	
	/**
	 * Check if the allocation percentual
	 * or the completed percentual aren't 100%
	 * 
	 * @return true = order is editable
	 */
	public boolean isEditable() {
		return allocationPercentual<100.0f && completePercentual<100.0f;
	}



	/**
	 * Can delete the order only
	 * if allocation percentual is 0
	 * and complete percentual is 0
	 * 
	 * @return true = a order can be deleted
	 */
	public boolean canDelete() {
		return allocationPercentual<=0.0f || completePercentual<=0.0f;
	}
}
