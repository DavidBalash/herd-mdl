/*
 * Copyright 2018 herd-mdl contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**/
package org.finra.herd.metastore.managed.jobProcessor;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.finra.herd.metastore.managed.JobDefinition;
import org.finra.herd.metastore.managed.conf.HerdMetastoreConfig;
import org.finra.herd.metastore.managed.datamgmt.DataMgmtSvc;
import org.finra.herd.sdk.invoker.ApiException;
import org.finra.herd.sdk.model.BusinessObjectFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ManagedObjStatsProcessor extends JobProcessor {
	private static final String SCRIPT_PATH = HerdMetastoreConfig.homeDir + "/metastor/deploy/common/scripts/stats/emr_gather_stats.sh";

	@Autowired
	DataMgmtSvc dataMgmtSvc;

	@Override
	protected ProcessBuilder createProcessBuilder( JobDefinition od ) {
		String dbName = od.getObjectDefinition().getDbName();
		String tblName = od.getObjectDefinition().getObjectName() + "_" + od.getObjectDefinition().getUsageCode() + "_" + od.getObjectDefinition().getFileType();
		tblName = tblName.replaceAll( "\\.", "_" ).replaceAll( " ", "_" ).replaceAll( "-", "_" );

		ProcessBuilder pb = null;
		try {
			BusinessObjectFormat dmFormat = dataMgmtSvc.getDMFormat( od );
			String quotedPartitionKeys = quotedPartitionKeys( dmFormat.getSchema() );
			if ( Strings.isNullOrEmpty( quotedPartitionKeys ) ) {
				log.error( "ERROR: PARTITION_COLUMNS is empty for {}", tblName );
				return pb;
			}
			pb = new ProcessBuilder( "sh", SCRIPT_PATH, dbName, tblName, quotedPartitionKeys );
		} catch ( ApiException e ) {
			log.error( "Could not get BO format due to: {}", e.getMessage(), e );
		}

		return pb;
	}
}
