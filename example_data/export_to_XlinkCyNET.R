folder_name = '/Users/XlinkCyNET/example_data/'

all_crosslinks=read.csv(paste0(folder_name,'input_file.csv',collapse=''),stringsAsFactors = F);
all_crosslinks$gene_ab=do.call(paste,c(all_crosslinks[,c('gene_a','gene_b')],sep='_'))

PPI_mtx = as.data.frame(matrix(0,nrow=length(table(all_crosslinks$gene_ab)),ncol=11))
colnames(PPI_mtx) = c('gene_a','gene_b','ppi_score','length_protein_a','length_protein_b','protein_a','protein_b','crosslinks_ab','crosslinks_ba','score_ab','score_ba')
gene_ab = unique(all_crosslinks$gene_ab)

for (i in 1:length(gene_ab)){
  PPI_mtx[i,'gene_a'] = strsplit(gene_ab[i],'_')[[1]][1]
  PPI_mtx[i,'gene_b'] = strsplit(gene_ab[i],'_')[[1]][2]
  PPI_mtx[i,'ppi_score'] = mean(apply(all_crosslinks[which(gene_ab[i] == all_crosslinks[,'gene_ab']),c('n_score_a_MS2_MS3','n_score_b_MS2_MS3')],1,max))
  PPI_mtx[i,'length_protein_a']= all_crosslinks[grep(PPI_mtx[i,'gene_a'],all_crosslinks$gene_a)[1],'length_protein_a']
  PPI_mtx[i,'length_protein_b']= all_crosslinks[grep(PPI_mtx[i,'gene_b'],all_crosslinks$gene_b)[1],'length_protein_b']
  PPI_mtx[i,'protein_a'] = all_crosslinks[grep(PPI_mtx[i,'gene_a'],all_crosslinks$gene_a)[1],'Protein1']
  PPI_mtx[i,'protein_b'] = all_crosslinks[grep(PPI_mtx[i,'gene_b'],all_crosslinks$gene_b)[1],'Protein2']
  for(j in 1:length(all_crosslinks[which(all_crosslinks$gene_a == PPI_mtx[i,'gene_a']), 'pro_site_ab'])) PPI_mtx[i,'crosslinks_ab'] = unique(paste(PPI_mtx[i,'crosslinks_ab'],all_crosslinks[which(all_crosslinks$gene_a == PPI_mtx[i,'gene_a'] ), 'pro_site_ab'][j],sep = '#'))
  for(j in 1:length(all_crosslinks[which(all_crosslinks$gene_b == PPI_mtx[i,'gene_b']), 'pro_site_ab'])) PPI_mtx[i,'crosslinks_ba'] = unique(paste(PPI_mtx[i,'crosslinks_ba'],all_crosslinks[which(all_crosslinks$gene_b == PPI_mtx[i,'gene_b'] ), 'pro_site_ab'][j],sep = '#'))
  for(j in 1:length(all_crosslinks[which(all_crosslinks$gene_a == PPI_mtx[i,'gene_a']), 'pro_site_ab'])) PPI_mtx[i,'score_ab'] = unique(paste(PPI_mtx[i,'score_ab'],max(all_crosslinks[which(all_crosslinks$gene_a == PPI_mtx[i,'gene_a'] ), 'n_score_a_MS2_MS3'][j],all_crosslinks[which(all_crosslinks$gene_a == PPI_mtx[i,'gene_a'] ), 'n_score_b_MS2_MS3'][j]),sep = '#'))
  for(j in 1:length(all_crosslinks[which(all_crosslinks$gene_b == PPI_mtx[i,'gene_b']), 'pro_site_ab'])) PPI_mtx[i,'score_ba'] = unique(paste(PPI_mtx[i,'score_ba'],max(all_crosslinks[which(all_crosslinks$gene_b == PPI_mtx[i,'gene_b'] ), 'n_score_a_MS2_MS3'][j],all_crosslinks[which(all_crosslinks$gene_b == PPI_mtx[i,'gene_b'] ), 'n_score_b_MS2_MS3'][j]),sep = '#'))
}

#remove '0#'
PPI_mtx = as.data.frame(sapply(PPI_mtx,sub,pattern="0#",replacement=""))
###########################################################################################

write.csv(PPI_mtx,file=paste0(folder_name,'/XlinkCyNET_input_file.csv',collapse=''),row.names=FALSE, quote = FALSE)