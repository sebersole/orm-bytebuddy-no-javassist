/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sebersole.bytebuddy_no_javassist;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.boot.MetadataSources;
import org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author Steve Ebersole
 */
public class BasicTest extends BaseNonConfigCoreFunctionalTestCase {
	@Test
	public void perform() {
		inTransaction(
				session -> session.persist( new TheEntity( 1, "a name" ) )
		);

		try {
			inTransaction(
					session -> {
						// get a proxy
						final TheEntity theEntity = session.load( TheEntity.class, 1 );

						// make sure it is a proxy
						assertFalse( Hibernate.isInitialized( theEntity ) );

						// force it to initialize
						Hibernate.initialize( theEntity );
					}
			);
		}
		finally {
			inTransaction(
					session -> session.createQuery( "delete TheEntity" ).executeUpdate()
			);
		}
	}

	@Override
	protected void addSettings(Map settings) {
		super.addSettings( settings );

		settings.put( AvailableSettings.BYTECODE_PROVIDER, BytecodeProviderImpl.class );
	}

	@Override
	protected void applyMetadataSources(MetadataSources sources) {
		super.applyMetadataSources( sources );
		sources.addAnnotatedClass( TheEntity.class );
	}

	@Entity( name = "TheEntity" )
	@Table( name = "entity_tbl" )
	public static class TheEntity {
		private Integer id;
		private String name;

		public TheEntity() {
		}

		public TheEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		@Id
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
