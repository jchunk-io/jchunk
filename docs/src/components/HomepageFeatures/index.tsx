import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Multiple Chunking Strategies',
    description: (
      <>
        Choose from fixed-size, recursive-character, and semantic chunkers to fit your use case and data.
      </>
    ),
  },
  {
    title: 'Simple Java API',
    description: (
      <>
        A clean, consistent interface across all chunkers. Configure with builders, then call <code>split(text)</code>.
      </>
    ),
  },
  {
    title: 'Retrieval Augmented Generation (RAG)',
    description: (
      <>
        Produce context-preserving chunks optimized for embeddings and retrieval pipelines in Java applications.
      </>
    ),
  },
];

function Feature({title, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className={clsx('card', styles.card, 'text--center')}>
        <div className="card__header">
          <Heading as="h3">{title}</Heading>
        </div>
        <div className="card__body">
          <p>{description}</p>
        </div>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={clsx('row', styles.featuresRow)}>
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
