/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.migrator.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.migrator.AbstractMigrationDescriptor;
import org.xwiki.contrib.migrator.MigrationDependencyManager;
import org.xwiki.contrib.migrator.MigrationException;

/**
 * This is the default implementation of the {@link MigrationDependencyManager}. 
 * The main function is graph the migration dependencies with a directed graph.
 * Topology sort is used to figure out the proper migration order according to dependencies.
 * 
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultMigrationDependencyManager implements MigrationDependencyManager
{
	@Inject
	private Logger logger;

	private HashMap<String, MigrationNode> migrationNodes = new HashMap<>();
	
	private HashSet<MigrationLink> migrationLinks = new HashSet<>();
	
	@Override
	public void createMigrationDependency(AbstractMigrationDescriptor preMigration, AbstractMigrationDescriptor postMigration) 
	{
		String preUUID = preMigration.getMigrationUUID();
		if (!migrationNodes.containsKey(preUUID)) {
			migrationNodes.put(preUUID, new MigrationNode(preMigration));
		}
		
		String postUUID = postMigration.getMigrationUUID();
		if (!migrationNodes.containsKey(postUUID)) {
			migrationNodes.put(postUUID, new MigrationNode(postMigration));
		}
		
		MigrationNode preMigrationNode = migrationNodes.get(preUUID);
		MigrationNode postMigrationNode = migrationNodes.get(postUUID);
		updateNodeDegree(preMigrationNode, postMigrationNode);
		MigrationLink migrationLink = createMigrationLink(preMigrationNode, postMigrationNode);
		migrationLinks.add(migrationLink);
	}

	@Override
	public List<AbstractMigrationDescriptor> getDependecyOrder() throws MigrationException
	{
		HashMap<MigrationNode, Integer> indegreeMap = new HashMap<>();
		Queue<MigrationNode> zeroIndegreeNodes = new LinkedList<>();
		for (MigrationNode migrationNode : migrationNodes.values()) {
			indegreeMap.put(migrationNode, migrationNode.indegree);
			if (migrationNode.indegree == 0) {
				zeroIndegreeNodes.add(migrationNode);
			}
		}
		
		if (zeroIndegreeNodes.isEmpty()) {
			logger.error("Failed to generate a dependency order for migration "
					+ "due to the cycle of dependencies");
		}
		
		List<AbstractMigrationDescriptor> dependencyOrder = new LinkedList<>();
		while (!zeroIndegreeNodes.isEmpty()) {
			MigrationNode migrationNode = zeroIndegreeNodes.poll();
			dependencyOrder.add(migrationNode.migrationDescriptor);
			for (MigrationNode nextNode : migrationNode.nexts) {
				indegreeMap.put(nextNode, indegreeMap.get(nextNode) - 1);
				if (indegreeMap.get(nextNode) == 0) {
					zeroIndegreeNodes.add(nextNode);
				}
			}
		}
		
		if (dependencyOrder.size() != migrationNodes.size()) {
			logger.error("Failed to generate a full size dependency order for "
					+ "migration due to the cycle of dependencies");
		}
		
		return dependencyOrder;
	}
	
	private class MigrationNode
	{
		public int indegree;
		public int outdegree;
		
		public ArrayList<MigrationNode> nexts;
		public ArrayList<MigrationLink> links;
		
		public AbstractMigrationDescriptor migrationDescriptor;

		public MigrationNode(AbstractMigrationDescriptor migrationDescriptor)
		{
			indegree = 0;
			outdegree = 0;
			nexts = new ArrayList<>();
			links = new ArrayList<>();
			this.migrationDescriptor = migrationDescriptor;
		}
	}
	
	private class MigrationLink
	{
		public MigrationNode preMigrationNode;
		public MigrationNode postMigrationNode;

		public MigrationLink(MigrationNode preMigrationNode, MigrationNode postMigrationNode)
		{
			this.preMigrationNode = preMigrationNode;
			this.postMigrationNode = postMigrationNode;
		}
	}
	
	private MigrationLink createMigrationLink(MigrationNode preMigrationNode, MigrationNode postMigrationNode) {
		MigrationLink migrationLink = new MigrationLink(preMigrationNode, postMigrationNode);
		preMigrationNode.links.add(migrationLink);
		return migrationLink;
	}
	
	private void updateNodeDegree(MigrationNode preMigrationNode, MigrationNode postMigrationNode) {
		preMigrationNode.nexts.add(postMigrationNode);
		preMigrationNode.outdegree++;
		postMigrationNode.indegree++;
	}
}
